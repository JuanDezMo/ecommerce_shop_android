package co.com.learningswift.shop.features.register

import co.com.learningswift.shop.core.errors.AppError

data class RegisterUiState(
    val form: RegisterForm = RegisterForm(),
    /** Un campo puede traer varios mensajes (los del backend): se muestran todos, uno por línea */
    val fieldErrors: Map<RegisterField, List<String>> = emptyMap(),
    /** Tras el primer intento de avanzar o enviar, cada campo se revalida al perder el foco */
    val hasSubmitted: Boolean = false,
    val isLoading: Boolean = false,
    /** Sube cada vez que hay que desplazarse al primer error: las pantallas lo observan */
    val scrollTick: Int = 0,
    /** El email recién registrado: la pantalla lo recoge para volver al login y lo consume */
    val registeredEmail: String? = null,
    val genericError: AppError? = null,
) {
    /** Campos con error de una pantalla (cuenta campos, no mensajes) */
    fun errorCount(step: RegisterStep): Int = fieldErrors.keys.count { it.step == step }

    val firstErrorField: RegisterField? get() = RegisterField.entries.firstOrNull { it in fieldErrors }

    val stepOfFirstError: RegisterStep? get() = firstErrorField?.step
}
