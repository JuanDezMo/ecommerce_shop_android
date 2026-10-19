package co.com.learningswift.shop.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class LoginResponseDto(val accessToken: String, val tokenType: String, val expiresIn: Int)
