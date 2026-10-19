package co.com.learningswift.shop.features.register

import java.time.LocalDate

/**
 * Reglas de validación del registro (Comportamiento §5.1). Puro: sin red ni Android, y con `today` como parámetro
 * para poder fijarlo en los tests. Devuelve UN mensaje por campo (el primero que falle).
 * La app puede ser más estricta que el backend, nunca más laxa.
 */
object RegisterValidator {
    private val namePattern = Regex("^[A-Za-z]+( [A-Za-z]+)*$")
    private val emailCharacters = Regex("^[A-Za-z0-9@.]+$")
    private val emailShape = Regex("^[A-Za-z0-9]+@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)+$")
    private val phonePattern = Regex("^\\+[0-9]{13}$")

    fun validate(form: RegisterForm, today: LocalDate): Map<RegisterField, String> {
        val errors = linkedMapOf<RegisterField, String>()
        fun add(field: RegisterField, message: String?) { if (message != null) errors[field] = message }

        add(RegisterField.FIRST_NAME, nameError(form.firstName, "first name", "First name"))
        add(RegisterField.LAST_NAME, nameError(form.lastName, "last name", "Last name"))
        add(RegisterField.EMAIL, emailError(form.email))
        add(RegisterField.PASSWORD, passwordError(form.password))
        add(RegisterField.PHONE, phoneError(form.phone))
        add(RegisterField.BIRTH_DATE, ageError(form, today))
        if (form.country == null) add(RegisterField.COUNTRY, "Select your country.")
        if (!form.termsAccepted) add(RegisterField.TERMS, "You must accept the terms and conditions.")
        return errors
    }

    private fun nameError(raw: String, lower: String, title: String): String? {
        val name = raw.trim()
        return when {
            name.isEmpty() -> "Enter your $lower."
            !namePattern.matches(name) -> "$title can only contain letters (A-Z, a-z) and single spaces between words."
            name.length !in 10..30 -> "$title must be 10 to 30 characters."
            else -> null
        }
    }

    private fun emailError(raw: String): String? {
        val email = raw.trim()
        return when {
            email.isEmpty() -> "Enter your email."
            !emailCharacters.matches(email) -> "Email can only contain letters, numbers, @ and a dot."
            !emailShape.matches(email) -> "Enter a valid email address."
            email.length !in 8..20 -> "Email must be 8 to 20 characters."
            else -> null
        }
    }

    /** La contraseña NO se recorta: un espacio cuenta (y es un error de composición) */
    private fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Enter a password."
        password.length !in 10..20 -> "Password must be 10 to 20 characters."
        !hasValidComposition(password) -> "Use an uppercase letter, a lowercase letter, a number and a special character, without spaces."
        else -> null
    }

    private fun hasValidComposition(password: String): Boolean =
        password.all { it in '!'..'~' } &&   // ASCII visible: sin espacios ni tildes
            password.any { it in 'a'..'z' } &&
            password.any { it in 'A'..'Z' } &&
            password.any { it in '0'..'9' } &&
            password.any { !(it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9') }

    private fun phoneError(raw: String): String? {
        val phone = raw.trim()
        return when {
            phone.isEmpty() -> "Enter your phone number."
            !phonePattern.matches(phone) -> "Use the format +CCC followed by 10 digits, for example +5733001234567."
            else -> null
        }
    }

    /**
     * Mayor de edad si (añoNac + 18, mes, día) <= (añoHoy, mesHoy, díaHoy), con "hoy" en UTC como el backend.
     * No se usa `plusYears(18)`: para el 29 de febrero devuelve el 28 y se pasaría por mayor un día antes que el backend.
     */
    private fun ageError(form: RegisterForm, today: LocalDate): String? {
        val adultOn = Triple(form.birthYear + 18, form.birthMonth, form.birthDay)
        val now = Triple(today.year, today.monthValue, today.dayOfMonth)
        val isAdult = compareValuesBy(adultOn, now, { it.first }, { it.second }, { it.third }) <= 0
        return if (isAdult) null else "You must be at least 18 years old to register."
    }
}
