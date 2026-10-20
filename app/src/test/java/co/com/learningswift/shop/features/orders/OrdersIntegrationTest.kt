package co.com.learningswift.shop.features.orders

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.model.CartItem
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.repository.AuthRepository
import co.com.learningswift.shop.data.repository.OrderRepository
import co.com.learningswift.shop.features.checkout.CheckoutForm
import co.com.learningswift.shop.support.MainDispatcherRule
import co.com.learningswift.shop.support.TestBackend
import java.math.BigDecimal
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule

/** Requieren el backend levantado (docker compose up en ecommerce_shop_api, perfil testing). Sin mocks: HTTP real */
class OrdersIntegrationTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val session = SessionManager()

    private suspend fun repository(email: String, baseUrl: String = TestBackend.BASE_URL): OrderRepository {
        val api = ApiClient(baseUrl, session)
        if (baseUrl == TestBackend.BASE_URL) AuthRepository(api, session).login(email, "Secret_123")
        return OrderRepository(api)
    }

    @BeforeTest fun resetBackend() = TestBackend.reset()

    // ---- lista ----
    @Test fun buyerHasTwoOrdersTheMostRecentFirst() = runTest {
        val viewModel = OrdersViewModel(repository("buyer@shoplab.dev"))
        viewModel.load().join()
        val orders = viewModel.state.value.orders
        assertEquals(listOf(2L, 1L), orders.map { it.id })
        assertEquals(listOf(BigDecimal("45.00"), BigDecimal("109.99")), orders.map { it.total })
        assertEquals(listOf(1, 3), orders.map { it.totalUnits })
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test fun aUserWithoutOrdersGetsAnEmptyList() = runTest {
        val viewModel = OrdersViewModel(repository("standard@shoplab.dev"))
        viewModel.load().join()
        assertTrue(viewModel.state.value.orders.isEmpty())
        assertTrue(viewModel.state.value.hasLoaded)
        assertNull(viewModel.state.value.genericError)
    }

    @Test fun aFreshPurchaseAppearsFirst() = runTest {
        val repository = repository("standard@shoplab.dev")
        val placed = repository.createOrder(
            listOf(CartItem(1, "Wireless Headphones", "headphones", BigDecimal("59.99"), 1)),
            CheckoutForm("Calle 10 #20-30", "Bogotá", "110111", Country.CO, PaymentMethod.CARD),
        )
        val viewModel = OrdersViewModel(repository)
        viewModel.load().join()
        assertEquals(placed.id, viewModel.state.value.orders.first().id)
    }

    @Test fun aListLoadErrorOpensTheGenericAlertAndTryAgainReloads() = runTest {
        val failing = OrdersViewModel(repository("standard@shoplab.dev", "http://localhost:9/api/v1/"))
        failing.load().join()
        assertEquals(AppError.Network, failing.state.value.genericError)
        failing.dismissGenericError()
        assertNull(failing.state.value.genericError)

        val working = OrdersViewModel(repository("buyer@shoplab.dev"))
        working.load().join()                          // "Try again" con el backend disponible
        assertEquals(2, working.state.value.orders.size)
        assertNull(working.state.value.genericError)
    }

    // ---- detalle ----
    @Test fun buyerOpensOrderOneWithItsDataFromTheBackend() = runTest {
        val viewModel = OrderDetailViewModel(repository("buyer@shoplab.dev"), 1)
        viewModel.load().join()
        val order = viewModel.state.value.order
        assertNotNull(order)
        assertEquals(1, order.id)
        assertEquals("CONFIRMED", order.status)
        assertEquals(BigDecimal("109.99"), order.total)
        assertEquals(listOf(BigDecimal("59.99"), BigDecimal("50.00")), order.items.map { it.subtotal })
        assertEquals("Calle 10 #20-30", order.shipping.address)
        assertEquals("Bogotá", order.shipping.city)
        assertEquals("110111", order.shipping.postalCode)
        assertTrue(order.shipping.country.isNotBlank())
    }

    @Test fun aForeignOrderOrAMissingOneIsNotFound() = runTest {
        val repository = repository("standard@shoplab.dev")
        for (id in listOf(1L, 999L)) {
            val viewModel = OrderDetailViewModel(repository, id)
            viewModel.load().join()
            assertTrue(viewModel.state.value.notFound, "orden $id")
            assertNull(viewModel.state.value.order)
            assertNull(viewModel.state.value.genericError)
        }
    }

    @Test fun anOrderDetailLoadErrorOpensTheGenericAlert() = runTest {
        val viewModel = OrderDetailViewModel(repository("standard@shoplab.dev", "http://localhost:9/api/v1/"), 1)
        viewModel.load().join()
        assertEquals(AppError.Network, viewModel.state.value.genericError)
        assertFalse(viewModel.state.value.notFound)
    }

    @Test fun dismissingNotFoundClearsIt() = runTest {
        val viewModel = OrderDetailViewModel(repository("standard@shoplab.dev"), 999)
        viewModel.load().join()
        viewModel.dismissNotFound()
        assertFalse(viewModel.state.value.notFound)
    }
}
