package co.com.learningswift.shop.features.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.LoadingOverlay
import co.com.learningswift.shop.core.design.ProductImageView
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.formatting.DisplayFormat
import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.Order
import co.com.learningswift.shop.data.model.OrderItem
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.model.Shipping
import co.com.learningswift.shop.data.repository.ThemeMode
import java.math.BigDecimal
import java.time.Instant

/** Sirve a la lista "My orders" y a la confirmación: `onBack` vuelve a donde se vino (también con "OK" de "Order not found") */
@Composable
fun OrderDetailScreen(viewModel: OrderDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    OrderDetailContent(
        state = state,
        onBack = onBack,
        onNotFoundOk = { viewModel.dismissNotFound(); onBack() },
        onRetry = { viewModel.load() },
        onDismissError = viewModel::dismissGenericError,
    )
}

/** Solo dibuja: sin ViewModel, para poder previsualizarlo */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OrderDetailContent(state: OrderDetailUiState, onBack: () -> Unit, onNotFoundOk: () -> Unit, onRetry: () -> Unit, onDismissError: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            state.order?.let { order ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Order #${order.id}", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag(TestTags.OrderDetail.ID_TEXT))
                    Text(DisplayFormat.status(order.status), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.testTag(TestTags.OrderDetail.STATUS_TEXT))
                    Text(DisplayFormat.detailDate(order.createdAt), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag(TestTags.OrderDetail.DATE_TEXT))
                    Text(order.paymentMethod.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag(TestTags.OrderDetail.PAYMENT_TEXT))

                    order.items.forEach { item -> OrderItemRow(item) }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        // El total es el del backend: la app no suma
                        Text(order.total.usd(), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag(TestTags.OrderDetail.TOTAL_TEXT))
                    }

                    Text("Shipping", style = MaterialTheme.typography.titleSmall)
                    val country = Country.entries.firstOrNull { it.code == order.shipping.country }?.displayName ?: order.shipping.country
                    Text(
                        listOf(order.shipping.address, order.shipping.city, order.shipping.postalCode, country).joinToString("\n"),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag(TestTags.OrderDetail.SHIPPING_TEXT),
                    )
                }
            }
        }
        if (state.isLoading && state.order == null) LoadingOverlay()

        if (state.notFound) {
            AlertDialog(
                onDismissRequest = {},   // la única salida es "OK"
                modifier = Modifier.semantics { testTagsAsResourceId = true },
                title = { Text(AlertText.OrderNotFound.TITLE) },
                text = { Text(AlertText.OrderNotFound.MESSAGE) },
                confirmButton = {
                    TextButton(onClick = onNotFoundOk, modifier = Modifier.testTag(TestTags.OrderDetail.NOT_FOUND_ALERT_OK_BUTTON)) {
                        Text(AlertText.OrderNotFound.OK)
                    }
                },
            )
        }
        state.genericError?.let { GenericErrorDialog(it, onRetry = onRetry, onDismiss = onDismissError) }
    }
}

/** Una línea de la orden. Contenedor con su ID y SIN fusionar a los hijos */
@Composable
private fun OrderItemRow(item: OrderItem) {
    Card(
        Modifier.fillMaxWidth().testTag(TestTags.OrderDetail.item(item.productId)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProductImageView(item.imageKey, Modifier.size(56.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text("${item.quantity} × ${item.unitPrice.usd()}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(item.subtotal.usd(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag(TestTags.OrderDetail.subtotal(item.productId)))
        }
    }
}

private val previewOrder = Order(
    1, "CONFIRMED", PaymentMethod.CARD,
    listOf(
        OrderItem(1, "SL-HEAD-001", "Wireless Headphones", "headphones", 1, BigDecimal("59.99"), BigDecimal("59.99")),
        OrderItem(4, "SL-MOUS-004", "Gaming Mouse", "mouse", 2, BigDecimal("25.00"), BigDecimal("50.00")),
    ),
    BigDecimal("109.99"), Shipping("Calle 10 #20-30", "Bogotá", "110111", "CO"), Instant.parse("2026-09-01T16:30:00Z"),
)

@Preview(name = "Detalle claro", showBackground = true, heightDp = 800)
@Composable
private fun DetailLightPreview() = ShopTheme(ThemeMode.LIGHT) { OrderDetailContent(OrderDetailUiState(previewOrder), {}, {}, {}, {}) }

@Preview(name = "Detalle oscuro", showBackground = true, heightDp = 800)
@Composable
private fun DetailDarkPreview() = ShopTheme(ThemeMode.DARK) { OrderDetailContent(OrderDetailUiState(previewOrder), {}, {}, {}, {}) }
