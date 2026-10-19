package co.com.learningswift.shop.integration

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.repository.ProductRepository
import co.com.learningswift.shop.support.TestBackend
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.BeforeTest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject

/** Requieren el backend levantado (docker compose up en ecommerce_shop_api, perfil testing). Sin mocks: HTTP real */
class ApiClientIntegrationTest {
    private val session = SessionManager()
    private val api = ApiClient(TestBackend.BASE_URL, session)
    private val products = ProductRepository(api)

    @BeforeTest fun resetBackend() = TestBackend.reset()

    @Test fun theSixSeedProductsArriveInOrder() = runTest {
        assertEquals(listOf(1L, 2, 3, 4, 5, 6), products.products().map { it.id })
    }

    @Test fun pricesAreExact() = runTest {
        val list = products.products()
        assertEquals(BigDecimal("59.99"), list[0].price)
        assertEquals(BigDecimal("25.00"), list[3].price)
    }

    @Test fun theSmartWatchIsSoldOut() = runTest {
        val list = products.products()
        assertTrue(list.first { it.id == 3L }.isSoldOut)
        assertFalse(list.first { it.id == 1L }.isSoldOut)
    }

    @Test fun aMissingProductIsMappedToProductNotFound() = runTest {
        val error = assertFailsWith<AppError.Api> { products.product(99) }
        assertEquals(404, error.status)
        assertEquals("PRODUCT_NOT_FOUND", error.code)
        assertEquals("This product is no longer available.", error.userMessage)
    }

    @Test fun anUnreachableBackendIsANetworkError() = runTest {
        val offline = ProductRepository(ApiClient("http://localhost:9/api/v1/", session))
        assertFailsWith<AppError.Network> { offline.products() }
    }

    @Test fun aRequestWithoutTokenToAProtectedRouteEndsTheSessionWithoutNotice() = runTest {
        session.start("abc.def")
        assertFailsWith<AppError.NotAuthenticated> { api.get<JsonObject>("users/me") }   // no se envía el token
        assertFalse(session.state.value.isLoggedIn)
        assertFalse(session.state.value.didExpire)
    }

    @Test fun aMalformedTokenExpiresTheSession() = runTest {
        session.start("not-a-valid-token")
        assertFailsWith<AppError.SessionExpired> { api.get<JsonObject>("users/me", authorized = true) }
        assertFalse(session.state.value.isLoggedIn)
        assertTrue(session.state.value.didExpire)
    }

    @Test fun aForcedErrorHeaderProducesARealGenericError() = runTest {
        val error = assertFailsWith<AppError.Api> {
            api.get<JsonObject>("products", headers = mapOf("X-Force-Error" to "503"))
        }
        assertEquals(503, error.status)
        assertEquals("Something went wrong on our side. Please try again later.", error.userMessage)
    }
}
