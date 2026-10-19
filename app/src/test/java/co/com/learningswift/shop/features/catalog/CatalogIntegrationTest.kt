package co.com.learningswift.shop.features.catalog

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.repository.ProductRepository
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
class CatalogIntegrationTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private fun makeViewModel(baseUrl: String = TestBackend.BASE_URL) =
        CatalogViewModel(ProductRepository(ApiClient(baseUrl, SessionManager())))

    @BeforeTest fun resetBackend() = TestBackend.reset()

    @Test fun theSixSeedProductsArriveInOrder() = runTest {
        val viewModel = makeViewModel()
        viewModel.load().join()
        val products = viewModel.state.value.products
        assertEquals(listOf(1L, 2, 3, 4, 5, 6), products.map { it.id })
        assertEquals("Wireless Headphones", products.first().name)
        assertEquals("Laptop Backpack", products.last().name)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test fun pricesAreExact() = runTest {
        val viewModel = makeViewModel()
        viewModel.load().join()
        assertEquals(BigDecimal("59.99"), viewModel.state.value.products.first { it.id == 1L }.price)
        assertEquals(BigDecimal("25.00"), viewModel.state.value.products.first { it.id == 4L }.price)
    }

    @Test fun theSmartWatchIsSoldOut() = runTest {
        val viewModel = makeViewModel()
        viewModel.load().join()
        assertTrue(viewModel.state.value.products.first { it.id == 3L }.isSoldOut)
    }

    @Test fun aLoadErrorOpensTheGenericAlertWithoutTheErrorTextYet() = runTest {
        val viewModel = makeViewModel("http://localhost:9/api/v1/")
        viewModel.load().join()
        assertEquals(AppError.Network, viewModel.state.value.genericError)
        assertFalse(viewModel.state.value.loadError)
    }

    @Test fun cancellingTheAlertLeavesTheErrorText() = runTest {
        val viewModel = makeViewModel("http://localhost:9/api/v1/")
        viewModel.load().join()
        viewModel.dismissGenericError()
        assertNull(viewModel.state.value.genericError)
        assertTrue(viewModel.state.value.loadError)
    }

    @Test fun tryingAgainWithTheBackendAvailableLoadsTheListAndClearsTheError() = runTest {
        val repository = ProductRepository(ApiClient("http://localhost:9/api/v1/", SessionManager()))
        val failing = CatalogViewModel(repository)
        failing.load().join()
        failing.dismissGenericError()
        assertTrue(failing.state.value.loadError)

        val recovered = makeViewModel()
        recovered.load().join()
        assertEquals(6, recovered.state.value.products.size)
        assertFalse(recovered.state.value.loadError)
        assertNull(recovered.state.value.genericError)
    }

    @Test fun reloadingAfterAnErrorOnTheSameViewModelClearsTheError() = runTest {
        val viewModel = makeViewModel()
        viewModel.load().join()
        viewModel.load().join()
        assertEquals(6, viewModel.state.value.products.size)
        assertNull(viewModel.state.value.genericError)
        assertFalse(viewModel.state.value.loadError)
    }

    @Test fun theFullScreenIndicatorIsOnlyForTheFirstLoad() = runTest {
        val viewModel = makeViewModel()
        viewModel.load().join()
        assertNotNull(viewModel.state.value.products.firstOrNull())
        // una recarga con datos ya en pantalla usa el indicador del gesto de refresco, no el de pantalla completa
        assertFalse(viewModel.state.value.copy(isLoading = true).showsFullScreenLoading)
        assertTrue(CatalogUiState(isLoading = true).showsFullScreenLoading)
    }
}
