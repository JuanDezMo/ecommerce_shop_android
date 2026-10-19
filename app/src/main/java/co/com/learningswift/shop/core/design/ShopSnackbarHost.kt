package co.com.learningswift.shop.core.design

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.repository.ThemeMode

/** Dibuja el mensaje de [SnackbarCenter]. Va en el `snackbarHost` del Scaffold: queda sobre la barra de pestañas */
@Composable
fun ShopSnackbarHost(snackbar: SnackbarCenter) {
    val message by snackbar.message.collectAsStateWithLifecycle()
    // Solo existe mientras hay mensaje: al desaparecer, el ID ya no está en el árbol
    message?.let { SnackbarPill(it) }
}

@Composable
fun SnackbarPill(text: String) {
    Surface(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).testTag(TestTags.App.SNACKBAR_TEXT),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(name = "Snackbar claro")
@Composable
private fun SnackbarLightPreview() = ShopTheme(ThemeMode.LIGHT) { SnackbarPill("Added to cart") }

@Preview(name = "Snackbar oscuro")
@Composable
private fun SnackbarDarkPreview() = ShopTheme(ThemeMode.DARK) { SnackbarPill("Added to cart") }
