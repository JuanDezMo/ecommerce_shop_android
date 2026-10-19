package co.com.learningswift.shop.core.errors

/**
 * Todo lo que puede salir mal en una operación de red, ya traducido por ApiClient.
 * Es una `sealed class`: un `when` sin `else` falla al compilar si se añade un error y no se trata.
 */
sealed class AppError : Exception() {
    data object Network : AppError()
    data object SessionExpired : AppError()      // AUTH_TOKEN_EXPIRED, _INVALID o _MALFORMED
    data object NotAuthenticated : AppError()    // AUTH_TOKEN_MISSING: la app no envió el token (bug)
    data object Unknown : AppError()
    data class Api(
        val status: Int,
        val code: String,
        /** Mensaje técnico del backend: NUNCA se muestra al usuario */
        val serverMessage: String,
        val fieldErrors: Map<String, List<String>>,
    ) : AppError()

    val userMessage: String get() = ErrorMessages.messageFor(this)
}
