package co.com.learningswift.shop.features.checkout

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.local.InMemoryStore
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.AuthRepository
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.OrderRepository
import co.com.learningswift.shop.support.MainDispatcherRule
import co.com.learningswift.shop.support.TestBackend
import java.math.BigDecimal
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule

/** Requieren el backend levantado (docker compose up en ecommerce_shop_api, perfil testing). Sin mocks: HTTP real. Crean órdenes */
class CheckoutIntegrationTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val session = SessionManager()
    private val cart = CartRepository(InMemoryStore())

    /** Un producto "como lo vio el usuario al añadirlo": con stock de sobra, aunque el backend ahora diga otra cosa */
    private fun seen(id: Long) = Product(id, "SKU-$id", "Product $id", "Description", BigDecimal("10.00"), stock = 99, imageKey = "mouse")

    private suspend fun makeViewModel(baseUrl: String = TestBackend.BASE_URL): CheckoutViewModel {
        val api = ApiClient(baseUrl, session)
        if (baseUrl == TestBackend.BASE_URL) AuthRepository(api, session).login("standard@shoplab.dev", "Secret_123")
        return CheckoutViewModel(cart, OrderRepository(api))
    }

    private fun CheckoutViewModel.fillValid() {
        onAddressChange("Calle 10 #20-30"); onCityChange("Bogotá"); onPostalCodeChange("110111"); onCountryChange(Country.CO)
    }

    private suspend fun CheckoutViewModel.buy() {
        placeOrderTapped()
        placeOrder().join()
    }

    @BeforeTest fun resetBackend() = TestBackend.reset()

    @Test fun twoProductsMakeAnOrderAndEmptyTheCart() = runTest {
        cart.add(seen(1), 2)
        cart.add(seen(4), 1)
        val viewModel = makeViewModel()
        viewModel.fillValid()
        viewModel.buy()
        val order = viewModel.state.value.placedOrder
        assertNotNull(order)
        assertEquals(BigDecimal("144.98"), order.total)
        assertEquals(3, order.totalUnits)
        assertTrue(cart.items.value.isEmpty())
        assertNull(viewModel.state.value.genericError)
        assertNull(viewModel.state.value.orderErrorMessage)
    }

    @Test fun aSoldOutProductIsRejectedAndTheCartIsKept() = runTest {
        cart.add(seen(3), 1)                    // el backend lo tiene con stock 0
        val viewModel = makeViewModel()
        viewModel.fillValid()
        viewModel.buy()
        assertEquals(
            "Some products in your cart don't have enough stock. Please update the quantities.",
            viewModel.state.value.orderErrorMessage,
        )
        assertNull(viewModel.state.value.placedOrder)
        assertEquals(1, cart.quantityOf(3))
    }

    @Test fun theLastUnitCanOnlyBeBoughtOnce() = runTest {
        val viewModel = makeViewModel()
        cart.add(seen(5), 1)
        viewModel.fillValid()
        viewModel.buy()
        assertNotNull(viewModel.state.value.placedOrder)

        val second = makeViewModel()
        cart.add(seen(5), 1)
        second.fillValid()
        second.buy()
        assertNull(second.state.value.placedOrder)
        assertNotNull(second.state.value.orderErrorMessage)
        assertEquals(1, cart.quantityOf(5))
    }

    @Test fun anEmptyFormShowsFourErrorsAndNeverCallsTheBackend() = runTest {
        cart.add(seen(1), 1)
        val viewModel = makeViewModel()
        viewModel.placeOrderTapped()
        val state = viewModel.state.value
        assertEquals(4, state.errorCount)
        assertFalse(state.isConfirmingOrder)
        assertEquals(1, state.scrollTick)
        assertNull(state.genericError)
        assertEquals(1, cart.quantityOf(1))
    }

    @Test fun validDataOpensTheConfirmationWithoutSendingAnything() = runTest {
        cart.add(seen(1), 1)
        val viewModel = makeViewModel()
        viewModel.fillValid()
        viewModel.placeOrderTapped()
        assertTrue(viewModel.state.value.isConfirmingOrder)
        assertNull(viewModel.state.value.placedOrder)
        viewModel.dismissPlaceOrder()           // "Cancel": vuelve al formulario con lo escrito
        assertFalse(viewModel.state.value.isConfirmingOrder)
        assertEquals("Bogotá", viewModel.state.value.form.city)
        assertEquals(1, cart.quantityOf(1))
    }

    @Test fun aFieldErrorOfTheBackendGoesUnderItsFieldAndTheCartIsKept() = runTest {
        cart.add(seen(1), 1)
        val viewModel = makeViewModel()
        val api = ApiClient(TestBackend.BASE_URL, session)
        val error = assertFailsWith<AppError.Api> {
            OrderRepository(api).createOrder(cart.items.value, CheckoutForm("Calle 10 #20-30", "A", "110111", Country.CO))
        }
        assertEquals("VALIDATION_ERROR", error.code)
        viewModel.handle(error)                 // lo que haría el ViewModel si la validación local se desalineara del backend
        assertNotNull(viewModel.state.value.fieldErrors[CheckoutField.CITY])
        assertNull(viewModel.state.value.orderErrorMessage)
        assertEquals(1, cart.quantityOf(1))
    }

    @Test fun anErrorWithoutAFormFieldOpensTheOrderErrorAlert() = runTest {
        val viewModel = makeViewModel()
        viewModel.handle(AppError.Api(400, "VALIDATION_ERROR", "x", mapOf("items[0].quantity" to listOf("must be at least 1"))))
        assertEquals("We couldn't process your cart. Please try again.", viewModel.state.value.orderErrorMessage)
        assertTrue(viewModel.state.value.fieldErrors.isEmpty())
    }

    @Test fun anUnreachableBackendOpensTheGenericAlertAndKeepsTheCart() = runTest {
        cart.add(seen(1), 2)
        val viewModel = makeViewModel(baseUrl = "http://localhost:9/api/v1/")
        viewModel.fillValid()
        viewModel.buy()
        assertEquals(AppError.Network, viewModel.state.value.genericError)
        assertEquals(2, cart.quantityOf(1))
        assertNull(viewModel.state.value.placedOrder)
    }

    @Test fun tryAgainReopensTheConfirmationInsteadOfResending() = runTest {
        cart.add(seen(1), 1)
        val viewModel = makeViewModel(baseUrl = "http://localhost:9/api/v1/")
        viewModel.fillValid()
        viewModel.buy()
        assertNotNull(viewModel.state.value.genericError)
        viewModel.dismissGenericError()
        viewModel.placeOrderTapped()            // lo que hace "Try again"
        assertTrue(viewModel.state.value.isConfirmingOrder)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.genericError)
        assertEquals(1, cart.quantityOf(1))
    }

    @Test fun twoQuickPlaceOrdersCreateOnlyOneOrder() = runTest {
        cart.add(seen(1), 1)
        val viewModel = makeViewModel()
        viewModel.fillValid()
        viewModel.placeOrderTapped()
        val first = viewModel.placeOrder()
        val second = viewModel.placeOrder()
        first.join(); second.join()
        val firstId = viewModel.state.value.placedOrder!!.id

        // si el segundo envío hubiera creado otra orden, la siguiente no sería firstId + 1
        val api = ApiClient(TestBackend.BASE_URL, session)
        val next = OrderRepository(api).createOrder(listOf(co.com.learningswift.shop.data.model.CartItem(1, "x", "mouse", BigDecimal.ONE, 1)), CheckoutForm("Calle 10 #20-30", "Bogotá", "110111", Country.CO))
        assertEquals(firstId + 1, next.id)
    }
}
