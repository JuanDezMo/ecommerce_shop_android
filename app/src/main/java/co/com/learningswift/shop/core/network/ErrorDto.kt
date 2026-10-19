package co.com.learningswift.shop.core.network

import kotlinx.serialization.Serializable

/** Cuerpo de error del backend: `code` estable, `message` técnico y `fieldErrors` (a veces varios mensajes por campo) */
@Serializable
data class ErrorDto(
    val code: String? = null,
    val message: String? = null,
    val fieldErrors: List<FieldErrorDto> = emptyList(),
)

@Serializable
data class FieldErrorDto(val field: String, val message: String)
