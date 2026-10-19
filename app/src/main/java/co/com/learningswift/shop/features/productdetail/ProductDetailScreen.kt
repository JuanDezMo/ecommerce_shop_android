package co.com.learningswift.shop.features.productdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.LoadingOverlay
import co.com.learningswift.shop.core.design.ProductImageView
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.design.SnackbarCenter
import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.ThemeMode
import java.math.BigDecimal

@Composable
fun ProductDetailScreen(viewModel: ProductDetailViewModel, cart: CartRepository, snackbar: SnackbarCenter, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cartItems by cart.items.collectAsStateWithLifecycle()   // para que el botón reaccione al carrito
    val flow = remember { AddToCartFlow(cart) }

    LaunchedEffect(Unit) { viewModel.load() }

    ProductDetailContent(
        state = state,
        // Se calcula aquí, donde se lee el carrito: así el botón reacciona cuando el carrito cambia
        action = cartItems.let { state.product?.let { product -> addToCartAction(product, cart) } } ?: AddToCartAction.Disabled,
        onBack = onBack,
        onAddTap = { state.product?.let(flow::start) },
        onRetry = { viewModel.load() },
        onDismissError = viewModel::dismissGenericError,
    )
    AddToCartHost(flow, cart, snackbar)
}

/** Solo dibuja: así se puede previsualizar. `action` dice qué hace el botón */
@Composable
fun ProductDetailContent(
    state: ProductDetailUiState,
    action: AddToCartAction,
    onBack: () -> Unit,
    onAddTap: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            state.product?.let { product ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp)) {
                    ProductImageView(product.imageKey, Modifier.fillMaxWidth(), product.isSoldOut, TestTags.ProductDetail.SOLD_OUT_TEXT)
                    Spacer(Modifier.height(16.dp))
                    Text(product.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag(TestTags.ProductDetail.NAME_TEXT))
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(product.price.usd(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(TestTags.ProductDetail.PRICE_TEXT))
                    }
                    Spacer(Modifier.height(4.dp))
                    // Con stock 0 no se repite "Sold out" aquí: ya sale sobre la imagen y su texto sustituye al del stock
                    if (!product.isSoldOut) {
                        Text("${product.stock} in stock", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag(TestTags.ProductDetail.STOCK_TEXT))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(product.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag(TestTags.ProductDetail.DESCRIPTION_TEXT))
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onAddTap,
                        enabled = action.isEnabled,
                        modifier = Modifier.fillMaxWidth().testTag(TestTags.ProductDetail.ADD_BUTTON),
                    ) { Text("Add to cart") }
                }
            }
        }
        if (state.isLoading && state.product == null) LoadingOverlay()
        state.genericError?.let { GenericErrorDialog(it, onRetry = onRetry, onDismiss = onDismissError) }
    }
}

private val previewProduct = Product(1, "SKU-1", "Wireless Headphones", "Over-ear Bluetooth headphones with noise cancelling.", BigDecimal("59.99"), 10, "headphones")

@Preview(name = "Detalle claro", showBackground = true, heightDp = 760)
@Composable
private fun DetailLightPreview() = ShopTheme(ThemeMode.LIGHT) {
    ProductDetailContent(ProductDetailUiState(previewProduct), AddToCartAction.OpenSheet, {}, {}, {}, {})
}

@Preview(name = "Detalle oscuro agotado", showBackground = true, heightDp = 760)
@Composable
private fun DetailDarkSoldOutPreview() = ShopTheme(ThemeMode.DARK) {
    ProductDetailContent(ProductDetailUiState(previewProduct.copy(stock = 0)), AddToCartAction.Disabled, {}, {}, {}, {})
}
