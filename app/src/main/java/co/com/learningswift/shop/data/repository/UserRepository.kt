package co.com.learningswift.shop.data.repository

import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.testing.ForcedError
import co.com.learningswift.shop.data.model.User
import co.com.learningswift.shop.data.remote.dto.RegisterRequestDto
import co.com.learningswift.shop.data.remote.dto.UserDto
import co.com.learningswift.shop.features.register.RegisterForm

class UserRepository(private val api: ApiClient) {
    /** Crea la cuenta (`POST users`). No inicia sesión: el usuario entra después desde el login */
    suspend fun register(form: RegisterForm): User {
        val dto: UserDto = api.post(
            path = "users",
            body = RegisterRequestDto.fromForm(form),
            headers = ForcedError.headersFor(form.email),
        )
        return dto.toModel()
    }

    /** El usuario con sesión (`GET users/me`, con token). Un token vencido lo gestiona ApiClient: cierra la sesión */
    suspend fun me(): User = api.get<UserDto>("users/me", authorized = true).toModel()
}
