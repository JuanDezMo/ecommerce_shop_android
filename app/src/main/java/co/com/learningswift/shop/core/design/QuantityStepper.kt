package co.com.learningswift.shop.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.com.learningswift.shop.data.repository.ThemeMode

/**
 * Selector de cantidad con "−" y "+" (Android no tiene Stepper). Los botones se llaman `<tag>-Decrement` y `<tag>-Increment`,
 * igual que los genera iOS, para reutilizar los flujos de Maestro. El valor lleva el ID `<tag>`.
 */
@Composable
fun QuantityStepper(value: Int, onValueChange: (Int) -> Unit, range: IntRange, tag: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { onValueChange(value - 1) },
            enabled = value > range.first,
            modifier = Modifier.testTag("$tag-Decrement"),
        ) { Text("−") }
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp).testTag(tag),
        )
        OutlinedButton(
            onClick = { onValueChange(value + 1) },
            enabled = value < range.last,
            modifier = Modifier.testTag("$tag-Increment"),
        ) { Text("+") }
    }
}

@Preview(name = "Selector claro", showBackground = true)
@Composable
private fun StepperLightPreview() = ShopTheme(ThemeMode.LIGHT) { QuantityStepper(3, {}, 1..5, "stepper") }

@Preview(name = "Selector oscuro", showBackground = true)
@Composable
private fun StepperDarkPreview() = ShopTheme(ThemeMode.DARK) { QuantityStepper(5, {}, 1..5, "stepper") }
