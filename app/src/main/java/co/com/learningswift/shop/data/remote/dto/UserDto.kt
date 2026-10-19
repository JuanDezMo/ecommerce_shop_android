package co.com.learningswift.shop.data.remote.dto

import co.com.learningswift.shop.data.model.Plan
import co.com.learningswift.shop.data.model.User
import kotlinx.serialization.Serializable

/** Usuario tal como lo devuelve el backend (registro y perfil). Nunca incluye la contraseña */
@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val birthDate: String,
    val country: String,
    val plan: Plan,
    val experienceLevel: Int,
    val newsletter: Boolean,
    val status: String,
) {
    fun toModel() = User(id, email, firstName, lastName, phone, birthDate, country, plan, experienceLevel, newsletter, status)
}
