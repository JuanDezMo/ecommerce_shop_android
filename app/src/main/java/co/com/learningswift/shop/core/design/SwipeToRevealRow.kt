package co.com.learningswift.shop.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import co.com.learningswift.shop.data.repository.ThemeMode
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val RevealWidth = 96.dp

/**
 * Fila que se desliza hacia la izquierda y deja a la vista un botón "Delete" a la espera de ser pulsado (como iOS).
 * Al soltar queda abierta si pasó la mitad del recorrido y cerrada si no. El botón solo existe mientras la fila está abierta
 * (o se está abriendo): así `..._delete_button` no aparece antes de deslizar.
 *
 * No se usa `SwipeToDismissBox`: o descarta al soltar o vuelve, y no deja un botón esperando.
 */
@Composable
fun SwipeToRevealRow(deleteTag: String, onDelete: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val revealPx = with(LocalDensity.current) { RevealWidth.toPx() }
    val offset = remember { Animatable(0f) }          // 0 = cerrada, -revealPx = abierta
    val scope = rememberCoroutineScope()

    Box(modifier) {
        if (offset.value < 0f) {
            Box(
                Modifier.matchParentSize().background(MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                TextButton(
                    onClick = { scope.launch { offset.snapTo(0f) }; onDelete() },
                    modifier = Modifier.width(RevealWidth).fillMaxHeight().testTag(deleteTag),
                ) { Text("Delete", color = MaterialTheme.colorScheme.onError) }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offset.snapTo((offset.value + delta).coerceIn(-revealPx, 0f)) }
                    },
                    onDragStopped = {
                        val target = if (offset.value < -revealPx / 2) -revealPx else 0f
                        scope.launch { offset.animateTo(target) }
                    },
                ),
        ) { content() }
    }
}

@Preview(name = "Fila cerrada", showBackground = true)
@Composable
private fun SwipeClosedPreview() = ShopTheme(ThemeMode.LIGHT) {
    SwipeToRevealRow("delete", {}) { Text("Wireless Headphones", Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer)) }
}
