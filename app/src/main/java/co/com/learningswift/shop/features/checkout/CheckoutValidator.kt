package co.com.learningswift.shop.features.checkout

/** Reglas del checkout (Comportamiento §8.1). Puro: sin red ni Android. Devuelve UN mensaje por campo (el primero que falle) */
object CheckoutValidator {
    private val postalCodePattern = Regex("^[A-Za-z0-9 -]{3,10}$")

    fun validate(form: CheckoutForm): Map<CheckoutField, String> {
        val errors = linkedMapOf<CheckoutField, String>()
        fun add(field: CheckoutField, message: String?) { if (message != null) errors[field] = message }

        add(CheckoutField.ADDRESS, textError(form.address, 5..120, "Enter your address.", "Address must be 5 to 120 characters."))
        add(CheckoutField.CITY, textError(form.city, 2..60, "Enter your city.", "City must be 2 to 60 characters."))
        add(CheckoutField.POSTAL_CODE, postalCodeError(form.postalCode))
        if (form.country == null) add(CheckoutField.COUNTRY, "Select your country.")
        return errors
    }

    private fun textError(raw: String, length: IntRange, empty: String, wrongLength: String): String? {
        val text = raw.trim()
        return when {
            text.isEmpty() -> empty
            text.length !in length -> wrongLength
            else -> null
        }
    }

    private fun postalCodeError(raw: String): String? {
        val code = raw.trim()
        return when {
            code.isEmpty() -> "Enter your postal code."
            !postalCodePattern.matches(code) -> "Use 3 to 10 letters, numbers, spaces or hyphens."
            else -> null
        }
    }
}
