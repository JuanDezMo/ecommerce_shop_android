package co.com.learningswift.shop.features.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.LoadingOverlay
import co.com.learningswift.shop.core.design.MessageBanner
import co.com.learningswift.shop.core.design.ProductImageView
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.design.SnackbarCenter
import co.com.learningswift.shop.core.design.clickableKeepingChildren
import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.ThemeMode
import co.com.learningswift.shop.features.productdetail.AddToCartAction
import co.com.learningswift.shop.features.productdetail.AddToCartFlow
import co.com.learningswift.shop.features.productdetail.AddToCartHost
import co.com.learningswift.shop.features.productdetail.addToCartAction
import java.math.BigDecimal

@Composable
fun CatalogScreen(viewModel: CatalogViewModel, cart: CartRepository, snackbar: SnackbarCenter, onProductTap: (Long) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cartItems by cart.items.collectAsStateWithLifecycle()   // para que los botones reaccionen al carrito
    val flow = remember { AddToCartFlow(cart) }

    // Al aparecer la pantalla (primera vez, al volver del detalle o de otra pestaña) se recarga la lista
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

    // Se calcula aquí (donde se lee el carrito) y no dentro de la lista: si no, las filas no se redibujan al cambiar el carrito
    val actions = cartItems.let { state.products.associate { it.id to addToCartAction(it, cart) } }

    CatalogContent(
        state = state,
        actions = actions,
        onRefresh = { viewModel.load() },
        onProductTap = onProductTap,
        onAddTap = flow::start,
        onRetry = { viewModel.load() },
        onDismissError = viewModel::dismissGenericError,
    )
    AddToCartHost(flow, cart, snackbar)
}

/** Solo dibuja: sin ViewModel ni carrito, para poder previsualizarlo. `actions` dice qué hace el botón de cada producto */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogContent(
    state: CatalogUiState,
    actions: Map<Long, AddToCartAction>,
    onRefresh: () -> Unit,
    onProductTap: (Long) -> Unit,
    onAddTap: (Product) -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text("Catalog", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            if (state.loadError) {
                MessageBanner(
                    "We couldn't load the products. Pull to refresh.",
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).testTag(TestTags.Catalog.ERROR_TEXT),
                    isError = true,
                )
            }
            PullToRefreshBox(isRefreshing = state.isLoading && state.products.isNotEmpty(), onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    Modifier.fillMaxSize().testTag(TestTags.Catalog.LIST),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductRow(product, actions[product.id] ?: AddToCartAction.Disabled, onProductTap, onAddTap)
                    }
                }
            }
        }
        if (state.showsFullScreenLoading) LoadingOverlay()
        state.genericError?.let { GenericErrorDialog(it, onRetry = onRetry, onDismiss = onDismissError) }
    }
}

/** Fila del catálogo. Es un contenedor con su ID y SIN fusionar a los hijos: así nombre, precio y botón conservan los suyos */
@Composable
private fun ProductRow(product: Product, action: AddToCartAction, onProductTap: (Long) -> Unit, onAddTap: (Product) -> Unit) {
    val id = product.id
    Card(
        Modifier.fillMaxWidth().testTag(TestTags.Catalog.item(id)).clickableKeepingChildren { onProductTap(id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProductImageView(product.imageKey, Modifier.size(84.dp), product.isSoldOut, TestTags.Catalog.soldOut(id))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag(TestTags.Catalog.name(id)))
                Text(product.price.usd(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag(TestTags.Catalog.price(id)))
            }
            // El botón consume su propio toque: no abre el detalle
            Button(onClick = { onAddTap(product) }, enabled = action.isEnabled, modifier = Modifier.testTag(TestTags.Catalog.addButton(id))) {
                Text("Add to cart")
            }
        }
    }
}

private fun sample(id: Long, name: String, stock: Int, key: String) =
    Product(id, "SKU-$id", name, "Description", BigDecimal("59.99"), stock, key)

private val previewProducts = listOf(
    sample(1, "Wireless Headphones", 10, "headphones"),
    sample(3, "Smart Watch", 0, "smartwatch"),
    sample(9, "Mystery Item", 4, "unknown"),
)

@Preview(name = "Catálogo claro", showBackground = true, heightDp = 640)
@Composable
private fun CatalogLightPreview() = ShopTheme(ThemeMode.LIGHT) {
    CatalogContent(CatalogUiState(previewProducts), previewProducts.associate { it.id to if (it.isSoldOut) AddToCartAction.Disabled else AddToCartAction.OpenSheet }, {}, {}, {}, {}, {})
}

@Preview(name = "Catálogo oscuro con error", showBackground = true, heightDp = 640)
@Composable
private fun CatalogDarkErrorPreview() = ShopTheme(ThemeMode.DARK) {
    CatalogContent(CatalogUiState(emptyList(), loadError = true), emptyMap(), {}, {}, {}, {}, {})
}
