package co.com.learningswift.shop.core.errors

/** `fields`: errores por campo del formulario. `hasUnmappedErrors`: hubo errores que no corresponden a ningún campo */
data class MappedFieldErrors<F>(val fields: Map<F, List<String>>, val hasUnmappedErrors: Boolean)

/** Traduce los `fieldErrors` del backend (por nombre) a los campos de un formulario. Genérico: lo reutiliza el checkout */
object FieldErrorMapper {
    fun <F> map(backend: Map<String, List<String>>, field: (String) -> F?): MappedFieldErrors<F> {
        val fields = linkedMapOf<F, List<String>>()
        var unmapped = backend.isEmpty()   // un VALIDATION_ERROR sin errores de campo también va a la alerta genérica
        for ((name, messages) in backend) {
            val mapped = field(name)
            if (mapped == null) unmapped = true else fields[mapped] = messages
        }
        return MappedFieldErrors(fields, unmapped)
    }
}
