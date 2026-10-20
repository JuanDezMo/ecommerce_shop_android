package co.com.learningswift.shop.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.SettingsRepository
import co.com.learningswift.shop.data.repository.ThemeMode
import co.com.learningswift.shop.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * La pestaña Account: perfil, tema y cierre de sesión. No conoce Android ni Compose.
 * `resetNavigation` lo da el contenedor: tras cerrar sesión, el siguiente login abre Catalog.
 */
class AccountViewModel(
    private val settings: SettingsRepository,
    private val userRepository: UserRepository,
    private val cart: CartRepository,
    private val session: SessionManager,
    private val resetNavigation: () -> Unit,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountUiState(themeMode = settings.themeMode.value))
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        // El tema vive en SettingsRepository: se refleja aquí para que la pantalla lo dibuje con el resto del estado
        viewModelScope.launch { settings.themeMode.collect { mode -> _state.update { it.copy(themeMode = mode) } } }
    }

    /** La pantalla lo llama cada vez que aparece. Devuelve el `Job` solo para los tests */
    fun loadProfile(): Job = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        try {
            val user = userRepository.me()
            _state.update { it.copy(user = user, genericError = null) }
        } catch (e: AppError) {
            // Con un token vencido ApiClient ya terminó la sesión y la raíz vuelve al login (con el aviso): no hay alerta
            if (e !is AppError.SessionExpired && e !is AppError.NotAuthenticated) _state.update { it.copy(genericError = e) }
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    /** Vacía el carrito, termina la sesión SIN aviso de expiración y reinicia la navegación */
    fun logout() {
        cart.clear()
        session.end()
        resetNavigation()
    }

    fun onThemeSelected(mode: ThemeMode) = settings.setThemeMode(mode)

    fun dismissGenericError() = _state.update { it.copy(genericError = null) }
}
