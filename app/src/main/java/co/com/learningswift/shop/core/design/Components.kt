package co.com.learningswift.shop.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.testing.TestTags

/** Indicador de carga a pantalla completa. Se usa dentro de un `if`: solo existe mientras carga */
@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(Modifier.testTag(TestTags.App.LOADING_INDICATOR))
    }
}

/** Aviso en una franja de color (sesión expirada, errores en línea) */
@Composable
fun MessageBanner(text: String, modifier: Modifier = Modifier, isError: Boolean = false) {
    val container = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer
    val content = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
    Box(modifier.background(container, RoundedCornerShape(12.dp)).padding(14.dp)) {
        Text(text, color = content, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Alerta "Something went wrong" con "Try again" y "Cancel". Cada ViewModel expone `genericError: AppError?`;
 * qué hace `onRetry` lo decide cada pantalla (Arquitectura §8.5).
 */
@Composable
fun GenericErrorDialog(error: AppError, onRetry: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AlertText.GenericError.TITLE) },
        text = { Text(error.userMessage) },
        confirmButton = {
            TextButton(onClick = { onDismiss(); onRetry() }, modifier = Modifier.testTag(TestTags.App.ERROR_ALERT_RETRY_BUTTON)) {
                Text(AlertText.GenericError.RETRY)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag(TestTags.App.ERROR_ALERT_CANCEL_BUTTON)) {
                Text(AlertText.GenericError.CANCEL)
            }
        },
    )
}

/** Contenido provisional de una pestaña que todavía no se ha construido */
@Composable
fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}
