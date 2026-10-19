package co.com.learningswift.shop.features.cart

import co.com.learningswift.shop.core.design.SnackbarCenter
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.local.InMemoryStore
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.ProductRepository
import co.com.learningswift.shop.support.MainDispatcherRule
import co.com.learningswift.shop.support.TestBackend
import java.math.BigDecimal
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule

/** Requieren el backend levantado (docker compose up en ecommerce_shop_api, perfil testing). Sin mocks: HTTP real */
class CartIntegrationTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val cart = CartRepository(InMemoryStore())

    /** Un producto "como lo vio el usuario al añadirlo": con stock de sobra, aunque el backend ahora diga otra cosa */
    private fun seen(id: Long, name: String = "Product $id") =
        Product(id, "SKU-$id", name, "Description", BigDecimal("10.00"), stock = 99, imageKey = "mouse")

    private fun makeViewModel(scope: CoroutineScope, baseUrl: String = TestBackend.BASE_URL) = CartViewModel(
        cart,
        ProductRepository(ApiClient(baseUrl, SessionManager())),
        SnackbarCenter(scope),
    )

    private val updatedMessage = "Your cart was updated to match available stock."

    @BeforeTest fun resetBackend() = TestBackend.reset()

    @Test fun aSoldOutProductDisappearsAndTheSnackbarAppears() = runTest {
        cart.add(seen(1), 2)
        cart.add(seen(3, "Smart Watch"), 1)     // el backend lo tiene con stock 0
        val viewModel = makeViewModel(backgroundScope)
        viewModel.refresh().join()
        assertEquals(listOf(1L), cart.items.value.map { it.productId })
        assertEquals(updatedMessage, viewModel.snackbar.message.value)
    }

    @Test fun nothingChangesAndNoSnackbarWhenThereIsEnoughStock() = runTest {
        cart.add(seen(5), 1)                    // el producto 5 tiene stock 1
        val viewModel = makeViewModel(backgroundScope)
        viewModel.refresh().join()
        assertEquals(1, cart.quantityOf(5))
        assertNull(viewModel.snackbar.message.value)
    }

    @Test fun aQuantityAboveTheStockIsLowered() = runTest {
        cart.add(seen(5), 5)
        val viewModel = makeViewModel(backgroundScope)
        viewModel.refresh().join()
        assertEquals(1, cart.quantityOf(5))
        assertEquals(updatedMessage, viewModel.snackbar.message.value)
    }

    @Test fun beforeTheFirstResponseTheSelectorDoesNotGoUp() = runTest {
        cart.add(seen(1), 2)
        val viewModel = makeViewModel(backgroundScope)
        val item = cart.items.value.single()
        assertEquals(2, viewModel.maxQuantity(item))
    }

    @Test fun afterTheResponseTheSelectorGoesUpToTheStockOrTheTwentyLimit() = runTest {
        cart.add(seen(1), 2)                    // stock real 10
        val viewModel = makeViewModel(backgroundScope)
        viewModel.refresh().join()
        assertEquals(10, viewModel.maxQuantity(cart.items.value.single()))

        cart.add(seen(2), 14)                   // 16 en total: la línea 1 solo puede subir 4 más (hasta 6)
        assertEquals(6, viewModel.maxQuantity(cart.items.value.first { it.productId == 1L }))
    }

    @Test fun aFailedRequestOpensTheGenericAlertAndLeavesTheCartAlone() = runTest {
        cart.add(seen(1), 2)
        cart.add(seen(3), 1)
        val viewModel = makeViewModel(backgroundScope, baseUrl = "http://localhost:9/api/v1/")
        viewModel.refresh().join()
        assertEquals(AppError.Network, viewModel.state.value.genericError)
        assertEquals(2, cart.items.value.size)
        assertEquals(2, cart.quantityOf(1))
        assertNull(viewModel.snackbar.message.value)
    }

    @Test fun tryAgainAdjustsTheCartAndClearsTheError() = runTest {
        cart.add(seen(3), 1)
        val failing = makeViewModel(backgroundScope, baseUrl = "http://localhost:9/api/v1/")
        failing.refresh().join()
        assertNotNull(failing.state.value.genericError)
        failing.dismissGenericError()
        assertNull(failing.state.value.genericError)

        val working = makeViewModel(backgroundScope)   // "Try again" con el backend disponible
        working.refresh().join()
        assertNull(working.state.value.genericError)
        assertEquals(0, cart.items.value.size)
    }

    @Test fun setQuantityRemoveAndClearGoThroughTheRepositoryWithTheKnownStock() = runTest {
        cart.add(seen(1), 2)
        cart.add(seen(4), 1)
        val viewModel = makeViewModel(backgroundScope)
        viewModel.refresh().join()
        viewModel.setQuantity(1, 4)
        assertEquals(4, cart.quantityOf(1))
        viewModel.setQuantity(1, 50)                 // stock real 10
        assertEquals(10, cart.quantityOf(1))
        viewModel.remove(4)
        assertEquals(listOf(1L), cart.items.value.map { it.productId })
        viewModel.clear()
        assertEquals(0, cart.items.value.size)
    }
}
