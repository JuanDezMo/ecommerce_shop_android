package co.com.learningswift.shop.core.design

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import co.com.learningswift.shop.data.repository.ThemeMode

/**
 * Tema de la app, claro u oscuro. `mode` es el que elige el usuario: SYSTEM sigue al dispositivo,
 * LIGHT y DARK lo fijan. No se usa el color dinámico de Android 12+ para que las dos plataformas se vean igual.
 */
@Composable
fun ShopTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val darkTheme = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Íconos de la barra de estado oscuros sobre fondo claro y claros sobre fondo oscuro
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
