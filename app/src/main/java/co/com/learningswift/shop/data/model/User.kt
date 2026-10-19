package co.com.learningswift.shop.data.model

/** Usuario como lo usa la app. `country` es texto y no [Country]: el backend también devuelve `ES`, que la app no ofrece */
data class User(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val birthDate: String,        // yyyy-MM-dd, se queda como texto
    val country: String,
    val plan: Plan,
    val experienceLevel: Int,
    val newsletter: Boolean,
    val status: String,
)
