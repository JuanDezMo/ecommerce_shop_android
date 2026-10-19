package co.com.learningswift.shop.features.register

enum class RegisterStep { BASICS, DETAILS }

/**
 * Los campos del registro, EN EL ORDEN del formulario: el orden decide cuál es "el primer campo con error".
 * Un solo sitio conoce los tres nombres de cada campo: el de la app, el del catálogo de IDs y el del backend.
 */
enum class RegisterField(val step: RegisterStep, val tagSuffix: String, val backendName: String) {
    FIRST_NAME(RegisterStep.BASICS, "firstname", "firstName"),
    LAST_NAME(RegisterStep.BASICS, "lastname", "lastName"),
    EMAIL(RegisterStep.BASICS, "email", "email"),
    PASSWORD(RegisterStep.BASICS, "password", "password"),
    PHONE(RegisterStep.BASICS, "phone", "phone"),
    BIRTH_DATE(RegisterStep.DETAILS, "birthdate", "birthDate"),
    COUNTRY(RegisterStep.DETAILS, "country", "country"),
    PLAN(RegisterStep.DETAILS, "plan", "plan"),
    EXPERIENCE(RegisterStep.DETAILS, "experience", "experienceLevel"),
    TERMS(RegisterStep.DETAILS, "terms", "termsAccepted");

    companion object {
        fun fromBackend(name: String): RegisterField? = entries.firstOrNull { it.backendName == name }
    }
}
