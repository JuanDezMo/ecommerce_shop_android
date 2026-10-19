package co.com.learningswift.shop.data.repository

import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.core.testing.ForcedError
import co.com.learningswift.shop.data.remote.dto.LoginRequestDto
import co.com.learningswift.shop.data.remote.dto.LoginResponseDto

class AuthRepository(private val api: ApiClient, private val session: SessionManager) {
    /**
     * Inicia sesión. El email se envía sin espacios en los extremos (el backend ya lo pasa a minúsculas)
     * y la contraseña tal como se escribió. El token va directo a la sesión: los ViewModels nunca lo ven.
     */
    suspend fun login(email: String, password: String) {
        val response: LoginResponseDto = api.post(
            path = "auth/login",
            body = LoginRequestDto(email.trim(), password),
            headers = ForcedError.headersFor(email),
        )
        session.start(response.accessToken)
    }
}
