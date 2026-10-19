package co.com.learningswift.shop.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) =
        // Vaciar el email borra el mensaje; cambiarlo sin vaciarlo lo conserva hasta el siguiente intento
        _state.update { it.copy(email = value, errorMessage = if (value.isBlank()) null else it.errorMessage) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value) }

    /** Devuelve el `Job` solo para que los tests esperen el resultado; la pantalla lo ignora */
    fun login(): Job = viewModelScope.launch {
        val current = _state.value
        if (!current.isLoginEnabled) return@launch          // evita un doble envío
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            authRepository.login(current.email, current.password)
            // No navega: la raíz cambia sola al detectar la sesión. Se vacía el formulario para que,
            // tras un logout, el login no muestre los datos anteriores (este ViewModel sobrevive al logout)
            _state.value = LoginUiState()
        } catch (e: AppError) {
            val inline = e is AppError.Api && e.code in INLINE_CODES
            _state.update {
                if (inline) it.copy(errorMessage = e.userMessage, isLoading = false)
                else it.copy(genericError = e, isLoading = false)
            }
        }
    }

    fun dismissGenericError() = _state.update { it.copy(genericError = null) }

    private companion object {
        val INLINE_CODES = setOf("INVALID_CREDENTIALS", "USER_LOCKED")
    }
}
