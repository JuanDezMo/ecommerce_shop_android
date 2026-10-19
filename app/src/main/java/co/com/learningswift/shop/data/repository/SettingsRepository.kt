package co.com.learningswift.shop.data.repository

import co.com.learningswift.shop.data.local.KeyValueStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Tema que elige el usuario. `SYSTEM` sigue la apariencia del dispositivo */
enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

/**
 * Preferencias del dispositivo. Expone el tema como StateFlow: la raíz de la app lo observa y todo cambia al instante.
 * No se borra al cerrar sesión: el tema es del dispositivo, no del usuario.
 */
class SettingsRepository(private val store: KeyValueStore) {
    private val _themeMode = MutableStateFlow(load())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        store.putString(KEY, mode.name)
        _themeMode.value = mode
    }

    // Un valor guardado que ya no existe cae en el sistema
    private fun load(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == store.getString(KEY) } ?: ThemeMode.SYSTEM

    private companion object {
        const val KEY = "settings.themeMode"
    }
}
