package co.com.learningswift.shop.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.learningswift.shop.data.repository.SettingsRepository
import co.com.learningswift.shop.data.repository.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Ejemplo del patrón de todas las pantallas: un UiState inmutable, expuesto como StateFlow de solo lectura,
 * y funciones públicas con nombre de acción. No conoce Android ni Compose. Por ahora solo gestiona el tema;
 * el perfil y el logout llegan en la iteración 9.
 */
class AccountViewModel(private val settings: SettingsRepository) : ViewModel() {

    val state: StateFlow<AccountUiState> = settings.themeMode
        .map { AccountUiState(themeMode = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountUiState(settings.themeMode.value))

    fun onThemeSelected(mode: ThemeMode) = settings.setThemeMode(mode)
}
