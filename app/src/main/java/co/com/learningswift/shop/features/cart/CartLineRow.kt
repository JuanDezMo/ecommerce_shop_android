package co.com.learningswift.shop.features.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.com.learningswift.shop.core.design.ProductImageView
import co.com.learningswift.shop.core.design.QuantityStepper
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.design.SwipeToRevealRow
import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.CartItem
import co.com.learningswift.shop.data.repository.ThemeMode
import java.math.BigDecimal

/** Una línea del carrito. Contenedor con su ID y SIN fusionar a los hijos: así nombre, selector y subtotal conservan los suyos */
@Composable
fun CartLineRow(item: CartItem, maxQuantity: Int, onQuantityChange: (Int) -> Unit, onDelete: () -> Unit) {
    val id = item.productId
    SwipeToRevealRow(deleteTag = TestTags.Cart.deleteButton(id), onDelete = onDelete, modifier = Modifier.testTag(TestTags.Cart.item(id))) {
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductImageView(item.imageKey, Modifier.size(64.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag(TestTags.Cart.name(id)))
                    QuantityStepper(item.quantity, onQuantityChange, 1..maxOf(item.quantity, maxQuantity), TestTags.Cart.quantityStepper(id))
                }
                Text(
                    (item.unitPrice * BigDecimal(item.quantity)).usd(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(TestTags.Cart.subtotal(id)),
                )
            }
        }
    }
}

@Preview(name = "Línea clara", showBackground = true)
@Composable
private fun LineLightPreview() = ShopTheme(ThemeMode.LIGHT) {
    CartLineRow(CartItem(1, "Wireless Headphones", "headphones", BigDecimal("59.99"), 2), 5, {}, {})
}

@Preview(name = "Línea oscura", showBackground = true)
@Composable
private fun LineDarkPreview() = ShopTheme(ThemeMode.DARK) {
    CartLineRow(CartItem(4, "Gaming Mouse", "mouse", BigDecimal("25.00"), 1), 1, {}, {})
}
