package co.com.learningswift.shop.core.design

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * Como `clickable`, pero SIN fusionar la semántica de los hijos. `clickable` usa `mergeDescendants = true`, y eso hace
 * desaparecer los IDs (`testTag`) de los elementos de dentro (nombre, precio, botón…) para Maestro y Appium.
 * Sigue siendo accesible: expone la acción de pulsar.
 */
fun Modifier.clickableKeepingChildren(onClick: () -> Unit): Modifier = this
    .pointerInput(onClick) { detectTapGestures(onTap = { onClick() }) }
    .semantics(mergeDescendants = false) {
        role = Role.Button
        onClick { onClick(); true }
    }
