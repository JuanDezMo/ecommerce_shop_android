package co.com.learningswift.shop.data.remote.dto

import co.com.learningswift.shop.features.register.RegisterForm
import java.util.Locale
import kotlinx.serialization.Serializable

/** Cuerpo de `POST users`. `phone` nulo no se envía (AppJson no envía nulos): un `""` sería un 400 */
@Serializable
data class RegisterRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String?,
    val birthDate: String,
    val country: String?,
    val plan: String,
    val experienceLevel: Int,
    val newsletter: Boolean,
    val termsAccepted: Boolean,
) {
    companion object {
        fun fromForm(form: RegisterForm): RegisterRequestDto {
            // El "+" del teléfono es solo de presentación: el backend admite únicamente dígitos
            val phone = form.phone.trim().removePrefix("+")
            return RegisterRequestDto(
                firstName = form.firstName.trim(),
                lastName = form.lastName.trim(),
                email = form.email.trim().lowercase(Locale.ROOT),
                password = form.password,   // tal como se escribió
                phone = phone.ifEmpty { null },
                // yyyy-MM-dd con los números elegidos; Locale.ROOT: nunca los dígitos de otro idioma
                birthDate = String.format(Locale.ROOT, "%04d-%02d-%02d", form.birthYear, form.birthMonth, form.birthDay),
                country = form.country?.code,
                plan = form.plan.name,
                experienceLevel = form.experienceLevel,
                newsletter = form.newsletter,
                termsAccepted = form.termsAccepted,
            )
        }
    }
}
