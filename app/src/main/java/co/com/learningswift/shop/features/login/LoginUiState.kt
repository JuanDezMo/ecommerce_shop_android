package co.com.learningswift.shop.features.login

import co.com.learningswift.shop.core.errors.AppError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    /** Credenciales incorrectas o cuenta bloqueada: se muestra en línea, bajo los campos */
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    /** Cualquier otro error: abre la alerta genérica */
    val genericError: AppError? = null,
) {
    val isLoginEnabled: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
