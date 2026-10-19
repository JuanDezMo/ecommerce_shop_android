package co.com.learningswift.shop.core.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Mismo azul de marca que el AccentColor de iOS: #2F5BDB en claro y #7A9BFF en oscuro.
// Todos los colores salen del tema: las pantallas nunca escriben colores fijos (regla del proyecto).
val LightColors = lightColorScheme(
    primary = Color(0xFF2F5BDB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE5FF),
    onPrimaryContainer = Color(0xFF001A5C),
    secondary = Color(0xFF5A6178),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDE5FF),
    onSecondaryContainer = Color(0xFF001A5C),
    surfaceContainer = Color(0xFFF1F1F6),
    surfaceContainerHigh = Color(0xFFE9E9EF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFF1F1F6),
    onSurfaceVariant = Color(0xFF5E5E66),
    outline = Color(0xFFC6C6CC),
    error = Color(0xFFD93025),
    onError = Color(0xFFFFFFFF),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF7A9BFF),
    onPrimary = Color(0xFF00205F),
    primaryContainer = Color(0xFF1F3EA6),
    onPrimaryContainer = Color(0xFFDDE5FF),
    secondary = Color(0xFFBFC5DC),
    onSecondary = Color(0xFF2A3042),
    secondaryContainer = Color(0xFF1F3EA6),
    onSecondaryContainer = Color(0xFFDDE5FF),
    surfaceContainer = Color(0xFF1C1C21),
    surfaceContainerHigh = Color(0xFF26262C),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE6E6EB),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE6E6EB),
    surfaceVariant = Color(0xFF1C1C21),
    onSurfaceVariant = Color(0xFFA9A9B2),
    outline = Color(0xFF45454D),
    error = Color(0xFFFF6B61),
    onError = Color(0xFF3B0A06),
)
