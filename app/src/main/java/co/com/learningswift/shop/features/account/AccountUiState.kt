package co.com.learningswift.shop.features.account

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.model.User
import co.com.learningswift.shop.data.repository.ThemeMode

/** Todo lo que la pantalla de cuenta dibuja. Inmutable: solo se cambia con copy() */
data class AccountUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val user: User? = null,
    val isLoading: Boolean = false,
    val genericError: AppError? = null,
)
