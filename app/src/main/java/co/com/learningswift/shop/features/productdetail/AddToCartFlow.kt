package co.com.learningswift.shop.features.productdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import co.com.learningswift.shop.core.design.QuantityStepper
import co.com.learningswift.shop.core.design.SnackbarCenter
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.CartRepository

/**
 * Estado de lo que provoca el botón "Add to cart": la hoja de cantidad o la alerta "Cart is full".
 * Cada pantalla con un botón de añadir tiene el suyo (`remember { AddToCartFlow(cart) }`) y dibuja [AddToCartHost].
 */
class AddToCartFlow(private val cart: CartRepository) {
    var sheetProduct by mutableStateOf<Product?>(null)
        internal set
    var showCartFull by mutableStateOf(false)
        internal set

    fun start(product: Product) {
        when (addToCartAction(product, cart)) {
            AddToCartAction.Disabled -> Unit
            AddToCartAction.CartFull -> showCartFull = true
            AddToCartAction.OpenSheet -> sheetProduct = product
        }
    }
}

/** Dibuja la hoja y la alerta que controla [flow] */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddToCartHost(flow: AddToCartFlow, cart: CartRepository, snackbar: SnackbarCenter) {
    flow.sheetProduct?.let { product ->
        ModalBottomSheet(
            onDismissRequest = { flow.sheetProduct = null },
            // La hoja es otra ventana: hay que repetir testTagsAsResourceId para que sus IDs lleguen a Maestro y Appium
            modifier = Modifier.semantics { testTagsAsResourceId = true },
        ) {
            AddToCartSheetContent(
                productName = product.name,
                maxQuantity = remember(product) { cart.maxAddable(product) },   // se calcula al abrir
                onConfirm = { quantity ->
                    cart.add(product, quantity)
                    flow.sheetProduct = null
                    snackbar.show(AlertText.Snackbar.ADDED_TO_CART)
                },
            )
        }
    }

    if (flow.showCartFull) {
        AlertDialog(
            onDismissRequest = { flow.showCartFull = false },
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            title = { Text(AlertText.CartFull.TITLE) },
            text = { Text(AlertText.CartFull.MESSAGE) },
            confirmButton = {
                TextButton(onClick = { flow.showCartFull = false }, modifier = Modifier.testTag(TestTags.App.CART_FULL_ALERT_OK_BUTTON)) {
                    Text(AlertText.CartFull.OK)
                }
            },
        )
    }
}

/** Contenido de la hoja "Add to cart": nombre, selector de 1 a lo que cabe y el botón de confirmar */
@Composable
fun AddToCartSheetContent(productName: String, maxQuantity: Int, onConfirm: (Int) -> Unit) {
    var quantity by remember { mutableIntStateOf(1) }

    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp).testTag(TestTags.AddToCart.SHEET),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(productName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(TestTags.AddToCart.NAME_TEXT))
        Spacer(Modifier.height(16.dp))
        QuantityStepper(quantity, { quantity = it }, 1..maxOf(1, maxQuantity), TestTags.AddToCart.QUANTITY_STEPPER)
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onConfirm(quantity) }, modifier = Modifier.fillMaxWidth().testTag(TestTags.AddToCart.CONFIRM_BUTTON)) {
            Text("Add to cart")
        }
        Spacer(Modifier.height(16.dp))
    }
}
