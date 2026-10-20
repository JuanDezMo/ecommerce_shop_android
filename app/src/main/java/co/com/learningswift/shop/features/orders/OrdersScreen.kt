package co.com.learningswift.shop.features.orders

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.LoadingOverlay
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.design.clickableKeepingChildren
import co.com.learningswift.shop.core.formatting.DisplayFormat
import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Order
import co.com.learningswift.shop.data.model.OrderItem
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.model.Shipping
import co.com.learningswift.shop.data.repository.ThemeMode
import java.math.BigDecimal
import java.time.Instant

@Composable
fun OrdersScreen(viewModel: OrdersViewModel, onOrderTap: (Long) -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Al aparecer la pantalla (primera vez o al volver del detalle) se recarga la lista
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

    OrdersContent(
        state = state,
        onRefresh = { viewModel.load() },
        onOrderTap = onOrderTap,
        onBack = onBack,
        onRetry = { viewModel.load() },
        onDismissError = viewModel::dismissGenericError,
    )
}

/** Solo dibuja: sin ViewModel, para poder previsualizarlo */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersContent(state: OrdersUiState, onRefresh: () -> Unit, onOrderTap: (Long) -> Unit, onBack: () -> Unit, onRetry: () -> Unit, onDismissError: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("My orders", style = MaterialTheme.typography.headlineSmall)
            }
            PullToRefreshBox(isRefreshing = state.isLoading && state.hasLoaded, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
                if (state.hasLoaded && state.orders.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "You haven't placed any orders yet.",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag(TestTags.Orders.EMPTY_TEXT),
                        )
                    }
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize().testTag(TestTags.Orders.LIST),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.orders, key = { it.id }) { order -> OrderRow(order, onOrderTap) }
                    }
                }
            }
        }
        if (state.showsFullScreenLoading) LoadingOverlay()
        state.genericError?.let { GenericErrorDialog(it, onRetry = onRetry, onDismiss = onDismissError) }
    }
}

/** Fila de una orden. Contenedor con su ID y SIN fusionar a los hijos: fecha, unidades y total conservan los suyos */
@Composable
private fun OrderRow(order: Order, onOrderTap: (Long) -> Unit) {
    Card(
        Modifier.fillMaxWidth().testTag(TestTags.Orders.item(order.id)).clickableKeepingChildren { onOrderTap(order.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium)
                Text(DisplayFormat.listDate(order.createdAt), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag(TestTags.Orders.date(order.id)))
                Text(DisplayFormat.units(order.totalUnits), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag(TestTags.Orders.count(order.id)))
            }
            Text(order.total.usd(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(TestTags.Orders.total(order.id)))
        }
    }
}

private fun sampleOrder(id: Long, units: Int, total: String) = Order(
    id, "CONFIRMED", PaymentMethod.CARD,
    listOf(OrderItem(1, "SKU", "Wireless Headphones", "headphones", units, BigDecimal(total), BigDecimal(total))),
    BigDecimal(total), Shipping("Calle 10 #20-30", "Bogotá", "110111", "CO"), Instant.parse("2026-09-15T15:00:00Z"),
)

@Preview(name = "Órdenes claro", showBackground = true, heightDp = 560)
@Composable
private fun OrdersLightPreview() = ShopTheme(ThemeMode.LIGHT) {
    OrdersContent(OrdersUiState(listOf(sampleOrder(2, 1, "45.00"), sampleOrder(1, 3, "109.99")), hasLoaded = true), {}, {}, {}, {}, {})
}

@Preview(name = "Órdenes vacío oscuro", showBackground = true, heightDp = 560)
@Composable
private fun OrdersEmptyDarkPreview() = ShopTheme(ThemeMode.DARK) { OrdersContent(OrdersUiState(hasLoaded = true), {}, {}, {}, {}, {}) }
