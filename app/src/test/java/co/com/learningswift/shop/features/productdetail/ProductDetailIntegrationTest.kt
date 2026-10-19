package co.com.learningswift.shop.features.productdetail

import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.repository.ProductRepository
import co.com.learningswift.shop.support.MainDispatcherRule
import co.com.learningswift.shop.support.TestBackend
import java.math.BigDecimal
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule

/** Requieren el backend levantado (docker compose up en ecommerce_shop_api, perfil testing). Sin mocks: HTTP real */
class ProductDetailIntegrationTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val repository = ProductRepository(ApiClient(TestBackend.BASE_URL, SessionManager()))

    @BeforeTest fun resetBackend() = TestBackend.reset()

    @Test fun productOneShowsItsData() = runTest {
        val viewModel = ProductDetailViewModel(repository, 1)
        viewModel.load().join()
        val product = viewModel.state.value.product
        assertNotNull(product)
        assertEquals("Wireless Headphones", product.name)
        assertEquals(BigDecimal("59.99"), product.price)
        assertEquals(10, product.stock)
        assertTrue(product.description.isNotBlank())
    }

    @Test fun productThreeIsSoldOut() = runTest {
        val viewModel = ProductDetailViewModel(repository, 3)
        viewModel.load().join()
        assertTrue(viewModel.state.value.product!!.isSoldOut)
    }

    @Test fun aMissingProductOpensTheAlertWithItsOwnMessage() = runTest {
        val viewModel = ProductDetailViewModel(repository, 99)
        viewModel.load().join()
        assertNull(viewModel.state.value.product)
        assertEquals("This product is no longer available.", viewModel.state.value.genericError?.userMessage)
    }

    @Test fun tryAgainRepeatsTheLoad() = runTest {
        val viewModel = ProductDetailViewModel(repository, 99)
        viewModel.load().join()
        viewModel.dismissGenericError()
        assertNull(viewModel.state.value.genericError)
        viewModel.load().join()                       // "Try again"
        assertNotNull(viewModel.state.value.genericError)
    }
}
