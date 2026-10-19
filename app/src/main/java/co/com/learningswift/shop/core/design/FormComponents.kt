package co.com.learningswift.shop.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.com.learningswift.shop.data.repository.ThemeMode

/** Error de un campo: una línea por mensaje, en rojo. Solo existe mientras hay error (así `..._error_text` desaparece al corregir) */
@Composable
fun FieldErrorText(lines: List<String>?, tag: String, modifier: Modifier = Modifier) {
    if (lines.isNullOrEmpty()) return
    Text(
        lines.joinToString("\n"),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.padding(start = 4.dp, top = 4.dp).testTag(tag),
    )
}

/** "1 field needs attention" / "3 fields need attention": cuenta campos, no mensajes */
@Composable
fun ErrorSummary(count: Int, tag: String, modifier: Modifier = Modifier) {
    if (count <= 0) return
    val text = if (count == 1) "1 field needs attention" else "$count fields need attention"
    MessageBanner(text, modifier.fillMaxWidth().testTag(tag), isError = true)
}

/** Casilla con texto. Se activa tocando toda la fila y expone su estado (`checked`/`unchecked`) a Maestro y Appium */
@Composable
fun LabeledCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String, tag: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .testTag(tag)
            .semantics { stateDescription = if (checked) "checked" else "unchecked" }
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Campo de texto del formulario con su error debajo. El `testTag` va en el campo (no en un contenedor)
 * y `onBlur` se llama al perder el foco, para revalidar.
 */
@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tag: String,
    errorTag: String,
    errors: List<String>?,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    supportingText: String? = null,
    onBlur: () -> Unit = {},
) {
    var wasFocused by remember { mutableStateOf(false) }
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = !errors.isNullOrEmpty(),
            supportingText = if (supportingText != null && errors.isNullOrEmpty()) ({ Text(supportingText) }) else null,
            // Sin autocorrección: el teclado cambiaría nombres y correos ("Alexandria" → "Alexandria's ")
            keyboardOptions = keyboardOptions.copy(autoCorrectEnabled = false),
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag)
                .onFocusChanged {
                    if (wasFocused && !it.isFocused) onBlur()
                    wasFocused = it.isFocused
                },
        )
        FieldErrorText(errors, errorTag)
    }
}

/** Fila de lista: etiqueta a la izquierda, valor a la derecha y un menú con las opciones al pulsarla */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownRow(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (index: Int) -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .testTag(tag)
                .heightIn(min = 52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(4.dp))
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded, modifier = Modifier.size(24.dp))
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(index); expanded = false })
            }
        }
    }
}

@Preview(name = "Errores claro", showBackground = true)
@Composable
private fun FormComponentsLightPreview() = ShopTheme(ThemeMode.LIGHT) { FormComponentsPreview() }

@Preview(name = "Errores oscuro", showBackground = true)
@Composable
private fun FormComponentsDarkPreview() = ShopTheme(ThemeMode.DARK) { FormComponentsPreview() }

@Composable
private fun FormComponentsPreview() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ErrorSummary(1, "summary1")
        ErrorSummary(3, "summary3")
        FormTextField("Email", "ana", {}, "t", "e", listOf("Enter a valid email address.", "Email must be 8 to 20 characters."))
        FormTextField("Phone", "", {}, "t2", "e2", null, supportingText = "For example +5733001234567")
        LabeledCheckbox(true, {}, "I accept the terms", "c1")
        LabeledCheckbox(false, {}, "I accept the terms", "c2")
        DropdownRow("Month", "September", listOf("September"), {}, "d")
    }
}
