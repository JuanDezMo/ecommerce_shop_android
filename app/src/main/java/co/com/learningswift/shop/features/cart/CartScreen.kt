package co.com.learningswift.shop.features.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.CartItem
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.ThemeMode
import java.math.BigDecimal

@Composable
fun CartScreen(viewModel: CartViewModel, cart: CartRepository, onCheckout: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by cart.items.collectAsStateWithLifecycle()

    // Cada vez que la pestaña aparece, el carrito se ajusta al stock real
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    CartContent(
        items = items,
        // Con `state` leído aquí, el máximo de cada línea se recalcula cuando llega el stock o cambia el carrito
        maxQuantityOf = { item -> state.let { viewModel.maxQuantity(item) } },
        genericError = state.genericError,
        onQuantityChange = viewModel::setQuantity,
        onDelete = viewModel::remove,
        onClear = viewModel::clear,
        onCheckout = onCheckout,
        onRetry = { viewModel.refresh() },
        onDismissError = viewModel::dismissGenericError,
    )
}

/** Solo dibuja: sin ViewModel ni repositorio, para poder previsualizarlo */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CartContent(
    items: List<CartItem>,
    maxQuantityOf: (CartItem) -> Int,
    genericError: co.com.learningswift.shop.core.errors.AppError?,
    onQuantityChange: (productId: Long, quantity: Int) -> Unit,
    onDelete: (productId: Long) -> Unit,
    onClear: () -> Unit,
    onCheckout: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
) {
    var showClearDialog by remember { mutableStateOf(false) }
    // Se calculan fuera del LazyColumn: así las filas se redibujan cuando cambia el carrito
    val maxima = items.associate { it.productId to maxQuantityOf(it) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text("Cart", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            if (items.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Your cart is empty",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag(TestTags.Cart.EMPTY_TEXT),
                    )
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth().testTag(TestTags.Cart.LIST),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.productId }) { item ->
                        CartLineRow(
                            item = item,
                            maxQuantity = maxima[item.productId] ?: item.quantity,
                            onQuantityChange = { onQuantityChange(item.productId, it) },
                            onDelete = { onDelete(item.productId) },
                        )
                    }
                }
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(
                            items.fold(BigDecimal.ZERO) { sum, item -> sum + item.unitPrice * BigDecimal(item.quantity) }.usd(),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.testTag(TestTags.Cart.TOTAL_TEXT),
                        )
                    }
                    Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().testTag(TestTags.Cart.CHECKOUT_BUTTON)) { Text("Checkout") }
                    OutlinedButton(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth().testTag(TestTags.Cart.CLEAR_BUTTON)) {
                        Text("Clear cart")
                    }
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                // El diálogo es otra ventana: hay que repetir testTagsAsResourceId para que sus IDs lleguen
                modifier = Modifier.semantics { testTagsAsResourceId = true },
                title = { Text(AlertText.ClearCart.TITLE) },
                confirmButton = {
                    TextButton(
                        onClick = { showClearDialog = false; onClear() },
                        modifier = Modifier.testTag(TestTags.Cart.CLEAR_DIALOG_CONFIRM_BUTTON),
                    ) { Text(AlertText.ClearCart.CONFIRM, color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }, modifier = Modifier.testTag(TestTags.Cart.CLEAR_DIALOG_CANCEL_BUTTON)) {
                        Text(AlertText.ClearCart.CANCEL)
                    }
                },
            )
        }
        genericError?.let { GenericErrorDialog(it, onRetry = onRetry, onDismiss = onDismissError) }
    }
}

private val previewItems = listOf(
    CartItem(1, "Wireless Headphones", "headphones", BigDecimal("59.99"), 2),
    CartItem(4, "Gaming Mouse", "mouse", BigDecimal("25.00"), 1),
)

@Preview(name = "Carrito claro", showBackground = true, heightDp = 700)
@Composable
private fun CartLightPreview() = ShopTheme(ThemeMode.LIGHT) { CartContent(previewItems, { 5 }, null, { _, _ -> }, {}, {}, {}, {}, {}) }

@Preview(name = "Carrito oscuro", showBackground = true, heightDp = 700)
@Composable
private fun CartDarkPreview() = ShopTheme(ThemeMode.DARK) { CartContent(previewItems, { 5 }, null, { _, _ -> }, {}, {}, {}, {}, {}) }

@Preview(name = "Carrito vacío", showBackground = true, heightDp = 500)
@Composable
private fun CartEmptyPreview() = ShopTheme(ThemeMode.LIGHT) { CartContent(emptyList(), { 0 }, null, { _, _ -> }, {}, {}, {}, {}, {}) }
