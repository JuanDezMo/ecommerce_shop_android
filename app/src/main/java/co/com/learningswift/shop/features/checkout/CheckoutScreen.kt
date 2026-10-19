package co.com.learningswift.shop.features.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.DropdownRow
import co.com.learningswift.shop.core.design.ErrorSummary
import co.com.learningswift.shop.core.design.FieldErrorText
import co.com.learningswift.shop.core.design.FormTextField
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.LoadingOverlay
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.ThemeMode
import java.math.BigDecimal

/**
 * `onOrderPlaced`: el backend creó la orden (201): navegar a la confirmación.
 * `onReviewCart`: "Review cart" en "Can't place your order": volver al carrito (que se ajusta al stock al aparecer).
 */
@Composable
fun CheckoutScreen(viewModel: CheckoutViewModel, cart: CartRepository, onOrderPlaced: () -> Unit, onReviewCart: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by cart.items.collectAsStateWithLifecycle()
    val estimatedTotal = items.fold(BigDecimal.ZERO) { sum, item -> sum + item.unitPrice * BigDecimal(item.quantity) }

    LaunchedEffect(state.placedOrder) { if (state.placedOrder != null) onOrderPlaced() }

    CheckoutContent(
        state = state,
        estimatedTotal = estimatedTotal,
        actions = CheckoutActions(
            onAddressChange = viewModel::onAddressChange,
            onCityChange = viewModel::onCityChange,
            onPostalCodeChange = viewModel::onPostalCodeChange,
            onCountryChange = viewModel::onCountryChange,
            onPaymentChange = viewModel::onPaymentChange,
            onFieldBlur = viewModel::onFieldBlur,
            onPlaceOrderTap = viewModel::placeOrderTapped,
            onConfirmPlaceOrder = { viewModel.placeOrder() },
            onCancelPlaceOrder = viewModel::dismissPlaceOrder,
            onReviewCart = { viewModel.dismissOrderError(); onReviewCart() },
            onDismissGenericError = viewModel::dismissGenericError,
            onBack = onBack,
        ),
    )
}

/** Lo que el formulario puede pedir. Agrupa las funciones para que [CheckoutContent] no dependa del ViewModel y se pueda previsualizar */
data class CheckoutActions(
    val onAddressChange: (String) -> Unit = {},
    val onCityChange: (String) -> Unit = {},
    val onPostalCodeChange: (String) -> Unit = {},
    val onCountryChange: (Country?) -> Unit = {},
    val onPaymentChange: (PaymentMethod) -> Unit = {},
    val onFieldBlur: (CheckoutField) -> Unit = {},
    val onPlaceOrderTap: () -> Unit = {},
    val onConfirmPlaceOrder: () -> Unit = {},
    val onCancelPlaceOrder: () -> Unit = {},
    val onReviewCart: () -> Unit = {},
    val onDismissGenericError: () -> Unit = {},
    val onBack: () -> Unit = {},
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CheckoutContent(state: CheckoutUiState, estimatedTotal: BigDecimal, actions: CheckoutActions) {
    val form = state.form
    val errors = state.fieldErrors
    val focusManager = LocalFocusManager.current
    val requesters = remember { CheckoutField.entries.associateWith { BringIntoViewRequester() } }

    // Tras un intento con errores, se desplaza al primer campo con error
    LaunchedEffect(state.scrollTick) {
        CheckoutField.entries.firstOrNull { it in errors }?.let { requesters.getValue(it).bringIntoView() }
    }

    fun field(f: CheckoutField) = Modifier.bringIntoViewRequester(requesters.getValue(f))
    fun errorTag(f: CheckoutField) = TestTags.Checkout.error(f.tagSuffix)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Column(
                Modifier.weight(1f).fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Checkout", style = MaterialTheme.typography.headlineMedium)
                ErrorSummary(state.errorCount, TestTags.Checkout.ERROR_SUMMARY_TEXT)

                FormTextField(
                    "Address", form.address, actions.onAddressChange, TestTags.Checkout.ADDRESS_INPUT,
                    errorTag(CheckoutField.ADDRESS), errors[CheckoutField.ADDRESS], field(CheckoutField.ADDRESS),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onBlur = { actions.onFieldBlur(CheckoutField.ADDRESS) },
                )
                FormTextField(
                    "City", form.city, actions.onCityChange, TestTags.Checkout.CITY_INPUT,
                    errorTag(CheckoutField.CITY), errors[CheckoutField.CITY], field(CheckoutField.CITY),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onBlur = { actions.onFieldBlur(CheckoutField.CITY) },
                )
                FormTextField(
                    "Postal code", form.postalCode, actions.onPostalCodeChange, TestTags.Checkout.POSTALCODE_INPUT,
                    errorTag(CheckoutField.POSTAL_CODE), errors[CheckoutField.POSTAL_CODE], field(CheckoutField.POSTAL_CODE),
                    // "Done" cierra el teclado y deja el botón "Place order" a la vista
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    onBlur = { actions.onFieldBlur(CheckoutField.POSTAL_CODE) },
                )

                Column(field(CheckoutField.COUNTRY)) {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                        val countries = Country.sorted
                        DropdownRow(
                            "Country", form.country?.displayName ?: "Select",
                            listOf("Select") + countries.map { it.displayName },
                            { index -> actions.onCountryChange(if (index == 0) null else countries[index - 1]) },
                            TestTags.Checkout.COUNTRY_DROPDOWN,
                        )
                    }
                    FieldErrorText(errors[CheckoutField.COUNTRY], errorTag(CheckoutField.COUNTRY))
                }

                Column(field(CheckoutField.PAYMENT)) {
                    Text("Payment method", style = MaterialTheme.typography.titleSmall)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag(TestTags.Checkout.PAYMENT_SEGMENTED)) {
                        PaymentMethod.entries.forEachIndexed { index, method ->
                            SegmentedButton(
                                selected = form.payment == method,
                                onClick = { actions.onPaymentChange(method) },
                                shape = SegmentedButtonDefaults.itemShape(index, PaymentMethod.entries.size),
                            ) { Text(method.label) }
                        }
                    }
                    FieldErrorText(errors[CheckoutField.PAYMENT], errorTag(CheckoutField.PAYMENT))
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Estimated total", style = MaterialTheme.typography.titleMedium)
                    Text(estimatedTotal.usd(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(TestTags.Checkout.TOTAL_TEXT))
                }
                // Siempre habilitado: así cada error se puede provocar y comprobar
                Button(onClick = actions.onPlaceOrderTap, modifier = Modifier.fillMaxWidth().testTag(TestTags.Checkout.CONFIRM_BUTTON)) {
                    Text("Place order")
                }
            }
        }

        if (state.isLoading) LoadingOverlay()

        if (state.isConfirmingOrder) {
            AlertDialog(
                onDismissRequest = actions.onCancelPlaceOrder,
                // Los diálogos son otra ventana: hay que repetir testTagsAsResourceId para que sus IDs lleguen
                modifier = Modifier.semantics { testTagsAsResourceId = true },
                title = { Text(AlertText.PlaceOrder.TITLE) },
                text = { Text(AlertText.PlaceOrder.message(estimatedTotal.usd())) },
                confirmButton = {
                    TextButton(onClick = actions.onConfirmPlaceOrder, modifier = Modifier.testTag(TestTags.Checkout.PLACE_ORDER_ALERT_CONFIRM_BUTTON)) {
                        Text(AlertText.PlaceOrder.CONFIRM)
                    }
                },
                dismissButton = {
                    TextButton(onClick = actions.onCancelPlaceOrder, modifier = Modifier.testTag(TestTags.Checkout.PLACE_ORDER_ALERT_CANCEL_BUTTON)) {
                        Text(AlertText.PlaceOrder.CANCEL)
                    }
                },
            )
        }

        state.orderErrorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = {},   // la única salida es "Review cart"
                modifier = Modifier.semantics { testTagsAsResourceId = true },
                title = { Text(AlertText.OrderError.TITLE) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = actions.onReviewCart, modifier = Modifier.testTag(TestTags.Checkout.ORDER_ERROR_ALERT_REVIEW_CART_BUTTON)) {
                        Text(AlertText.OrderError.REVIEW_CART)
                    }
                },
            )
        }

        // "Try again" NO reenvía: reabre "Place order?" (el backend no es idempotente)
        state.genericError?.let { GenericErrorDialog(it, onRetry = actions.onPlaceOrderTap, onDismiss = actions.onDismissGenericError) }
    }
}

@Preview(name = "Checkout claro", showBackground = true, heightDp = 900)
@Composable
private fun CheckoutLightPreview() = ShopTheme(ThemeMode.LIGHT) { CheckoutContent(CheckoutUiState(), BigDecimal("144.98"), CheckoutActions()) }

@Preview(name = "Checkout oscuro con errores", showBackground = true, heightDp = 900)
@Composable
private fun CheckoutDarkErrorsPreview() = ShopTheme(ThemeMode.DARK) {
    val errors = CheckoutField.entries.filter { it != CheckoutField.PAYMENT }.associateWith { listOf("Enter this field.") }
    CheckoutContent(CheckoutUiState(fieldErrors = errors), BigDecimal("144.98"), CheckoutActions())
}
