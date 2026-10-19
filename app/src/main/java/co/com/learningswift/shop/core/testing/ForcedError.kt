package co.com.learningswift.shop.core.testing

import co.com.learningswift.shop.BuildConfig

/**
 * Solo debug: un email `force503@shoplab.dev` hace que el login lleve la cabecera `X-Force-Error: 503`
 * y el backend (perfil `testing`) responda ese error. Sirve para ver la alerta genérica sin apagar el backend.
 * No es un mock: la respuesta la sigue dando el backend real. En release no hace nada.
 */
object ForcedError {
    private val pattern = Regex("^force(\\d{3})@.*$", RegexOption.IGNORE_CASE)

    fun status(email: String): Int? {
        if (!BuildConfig.DEBUG) return null
        val status = pattern.matchEntire(email.trim())?.groupValues?.get(1)?.toInt() ?: return null
        return if (status in 400..599) status else null
    }

    fun headersFor(email: String): Map<String, String> =
        status(email)?.let { mapOf("X-Force-Error" to it.toString()) } ?: emptyMap()
}
