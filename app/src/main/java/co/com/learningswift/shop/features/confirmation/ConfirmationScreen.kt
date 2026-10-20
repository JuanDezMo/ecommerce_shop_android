package co.com.learningswift.shop.features.confirmation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.com.learningswift.shop.core.design.ProductImageView
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Order
import co.com.learningswift.shop.data.model.OrderItem
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.model.Shipping
import co.com.learningswift.shop.data.repository.ThemeMode
import java.math.BigDecimal

/** La compra terminó: todo sale de la respuesta 201 (sin otra consulta). No hay forma de volver atrás al formulario */
@Composable
fun ConfirmationScreen(order: Order, onViewOrder: () -> Unit, onDone: () -> Unit) {
    // Ni la flecha ni el gesto o el botón de atrás del sistema: la orden ya se creó y volver al formulario podría duplicarla
    BackHandler(enabled = true) {}

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Order confirmed", style = MaterialTheme.typography.headlineMedium)
        Text("Order #${order.id}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(TestTags.Confirmation.ORDER_ID_TEXT))

        order.items.forEach { item ->
            // Contenedor con su ID y SIN fusionar a los hijos
            Card(
                Modifier.fillMaxWidth().testTag(TestTags.Confirmation.item(item.productId)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProductImageView(item.imageKey, Modifier.size(56.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text("Quantity: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(item.subtotal.usd(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total", style = MaterialTheme.typography.titleMedium)
            Text(order.total.usd(), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag(TestTags.Confirmation.TOTAL_TEXT))
        }

        Button(onClick = onViewOrder, modifier = Modifier.fillMaxWidth().testTag(TestTags.Confirmation.VIEW_ORDER_BUTTON)) { Text("View order") }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().testTag(TestTags.Confirmation.DONE_BUTTON)) { Text("Done") }
    }
}

private val previewOrder = Order(
    id = 3, status = "CONFIRMED", paymentMethod = PaymentMethod.CARD,
    items = listOf(
        OrderItem(1, "SL-HEAD-001", "Wireless Headphones", "headphones", 2, BigDecimal("59.99"), BigDecimal("119.98")),
        OrderItem(4, "SL-MOUS-004", "Gaming Mouse", "mouse", 1, BigDecimal("25.00"), BigDecimal("25.00")),
    ),
    total = BigDecimal("144.98"),
    shipping = Shipping("Calle 10 #20-30", "Bogotá", "110111", "CO"),
    createdAt = java.time.Instant.parse("2026-10-14T15:04:05Z"),
)

@Preview(name = "Confirmación clara", showBackground = true, heightDp = 800)
@Composable
private fun ConfirmationLightPreview() = ShopTheme(ThemeMode.LIGHT) { ConfirmationScreen(previewOrder, {}, {}) }

@Preview(name = "Confirmación oscura", showBackground = true, heightDp = 800)
@Composable
private fun ConfirmationDarkPreview() = ShopTheme(ThemeMode.DARK) { ConfirmationScreen(previewOrder, {}, {}) }
