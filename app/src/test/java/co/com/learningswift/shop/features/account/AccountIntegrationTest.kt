package co.com.learningswift.shop.features.account

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.local.InMemoryStore
import co.com.learningswift.shop.data.model.Plan
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.AuthRepository
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.SettingsRepository
import co.com.learningswift.shop.data.repository.ThemeMode
import co.com.learningswift.shop.data.repository.UserRepository
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
class AccountIntegrationTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val session = SessionManager()
    private val store = InMemoryStore()
    private val settings = SettingsRepository(store)
    private val cart = CartRepository(store)
    private var navigationResets = 0

    private fun product(id: Long) = Product(id, "SKU-$id", "Product $id", "Description", BigDecimal("10.00"), 99, "mouse")

    private suspend fun makeViewModel(baseUrl: String = TestBackend.BASE_URL, login: Boolean = true): AccountViewModel {
        val api = ApiClient(baseUrl, session)
        if (login) AuthRepository(ApiClient(TestBackend.BASE_URL, session), session).login("standard@shoplab.dev", "Secret_123")
        return AccountViewModel(settings, UserRepository(api), cart, session) { navigationResets++ }
    }

    @BeforeTest fun resetBackend() = TestBackend.reset()

    @Test fun theProfileOfTheSeedUserIsLoaded() = runTest {
        val viewModel = makeViewModel()
        viewModel.loadProfile().join()
        val user = viewModel.state.value.user
        assertNotNull(user)
        assertEquals("standard@shoplab.dev", user.email)
        assertTrue(user.firstName.startsWith("Standard"))
        assertEquals(Plan.BASIC, user.plan)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.genericError)
    }

    @Test fun logoutEmptiesTheCartEndsTheSessionWithoutNoticeAndResetsTheNavigation() = runTest {
        cart.add(product(1), 3)
        val viewModel = makeViewModel()
        viewModel.logout()
        assertTrue(cart.items.value.isEmpty())
        assertFalse(session.state.value.isLoggedIn)
        assertFalse(session.state.value.didExpire)
        assertEquals(1, navigationResets)
    }

    @Test fun anExpiredTokenEndsTheSessionWithNoticeKeepsTheCartAndShowsNoAlert() = runTest {
        cart.add(product(1), 3)
        val viewModel = makeViewModel()
        session.start(TestBackend.expiredToken("standard@shoplab.dev"))
        viewModel.loadProfile().join()
        assertFalse(session.state.value.isLoggedIn)
        assertTrue(session.state.value.didExpire)
        assertEquals(3, cart.quantityOf(1))
        assertNull(viewModel.state.value.genericError)       // la raíz vuelve al login: no hay alerta
        assertEquals(0, navigationResets)
    }

    @Test fun anUnreachableBackendOpensTheGenericAlertAndTryAgainLoadsTheProfile() = runTest {
        val failing = makeViewModel(baseUrl = "http://localhost:9/api/v1/")
        failing.loadProfile().join()
        assertEquals(AppError.Network, failing.state.value.genericError)
        failing.dismissGenericError()
        assertNull(failing.state.value.genericError)

        val working = AccountViewModel(settings, UserRepository(ApiClient(TestBackend.BASE_URL, session)), cart, session) { }
        working.loadProfile().join()                        // "Try again" con el backend disponible
        assertNotNull(working.state.value.user)
        assertNull(working.state.value.genericError)
    }

    @Test fun theChosenThemeShowsInTheStateAndSurvivesLogout() = runTest {
        val viewModel = makeViewModel()
        viewModel.onThemeSelected(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.state.value.themeMode)
        viewModel.logout()
        assertEquals(ThemeMode.DARK, settings.themeMode.value)
        assertEquals(ThemeMode.DARK, SettingsRepository(store).themeMode.value)
    }
}
