package co.com.learningswift.shop.features.login

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.repository.AuthRepository
import co.com.learningswift.shop.support.MainDispatcherRule
import co.com.learningswift.shop.support.TestBackend
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.Rule

/** Requieren el backend levantado (docker compose up en ecommerce_shop_api, perfil testing). Sin mocks: HTTP real */
class LoginIntegrationTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val session = SessionManager()
    private fun makeViewModel(baseUrl: String = TestBackend.BASE_URL) =
        LoginViewModel(AuthRepository(ApiClient(baseUrl, session), session))

    private suspend fun LoginViewModel.loginWith(email: String, password: String) {
        onEmailChange(email)
        onPasswordChange(password)
        login().join()
    }

    @BeforeTest fun resetBackend() = TestBackend.reset()

    @Test fun anActiveUserStartsASession() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("standard@shoplab.dev", "Secret_123")
        assertTrue(session.state.value.isLoggedIn)
        assertNull(viewModel.state.value.errorMessage)
        assertNull(viewModel.state.value.genericError)
    }

    @Test fun aWrongPasswordShowsTheInlineMessage() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("standard@shoplab.dev", "Wrong_123")
        assertEquals("The email or password is incorrect.", viewModel.state.value.errorMessage)
        assertFalse(session.state.value.isLoggedIn)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.isLoginEnabled)
        assertNull(viewModel.state.value.genericError)
    }

    @Test fun aLockedUserWithTheRightPasswordShowsTheLockedMessage() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("locked@shoplab.dev", "Secret_123")
        assertEquals("Your account is locked. Please contact support.", viewModel.state.value.errorMessage)
        assertFalse(session.state.value.isLoggedIn)
    }

    @Test fun aLockedUserWithAWrongPasswordLooksLikeAnyWrongCredential() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("locked@shoplab.dev", "Wrong_123")
        assertEquals("The email or password is incorrect.", viewModel.state.value.errorMessage)
    }

    @Test fun theEmailIsTrimmedAndCaseInsensitive() = runTest {
        makeViewModel().loginWith("  Standard@ShopLab.dev ", "Secret_123")
        assertTrue(session.state.value.isLoggedIn)
    }

    @Test fun aForcedFiveHundredErrorOpensTheGenericAlert() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("force503@shoplab.dev", "anything")
        assertEquals("Something went wrong on our side. Please try again later.", viewModel.state.value.genericError?.userMessage)
        assertNull(viewModel.state.value.errorMessage)
        assertFalse(session.state.value.isLoggedIn)
    }

    @Test fun aForcedFourHundredErrorOpensTheGenericAlert() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("force418@shoplab.dev", "anything")
        assertEquals("Something went wrong. Please try again.", viewModel.state.value.genericError?.userMessage)
    }

    @Test fun anUnreachableBackendOpensTheGenericAlert() = runTest {
        val viewModel = makeViewModel(baseUrl = "http://localhost:9/api/v1/")
        viewModel.loginWith("standard@shoplab.dev", "Secret_123")
        assertEquals(AppError.Network, viewModel.state.value.genericError)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test fun retryingAfterTheAlertKeepsTheTypedData() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("force503@shoplab.dev", "anything")
        viewModel.dismissGenericError()
        assertNull(viewModel.state.value.genericError)
        assertEquals("force503@shoplab.dev", viewModel.state.value.email)
        viewModel.login().join()   // lo que hace "Try again"
        assertTrue(viewModel.state.value.genericError != null)
    }

    @Test fun theMessageIsReplacedByTheNextAttempt() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("standard@shoplab.dev", "Wrong_123")
        viewModel.loginWith("locked@shoplab.dev", "Secret_123")
        assertEquals("Your account is locked. Please contact support.", viewModel.state.value.errorMessage)
        viewModel.loginWith("standard@shoplab.dev", "Secret_123")
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test fun clearingTheEmailClearsTheMessageButEditingItDoesNot() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("standard@shoplab.dev", "Wrong_123")
        viewModel.onEmailChange("standard@shoplab.de")
        assertEquals("The email or password is incorrect.", viewModel.state.value.errorMessage)
        viewModel.onEmailChange("   ")
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test fun theFormIsEmptyAfterASuccessfulLogin() = runTest {
        val viewModel = makeViewModel()
        viewModel.loginWith("standard@shoplab.dev", "Secret_123")
        assertEquals("", viewModel.state.value.email)
        assertEquals("", viewModel.state.value.password)
    }

    @Test fun anExpiredTokenExpiresTheSessionAndAnewLoginClearsTheFlag() = runTest {
        val api = ApiClient(TestBackend.BASE_URL, session)
        session.start(TestBackend.expiredToken("standard@shoplab.dev"))
        assertFailsWith<AppError.SessionExpired> { api.get<JsonObject>("users/me", authorized = true) }
        assertTrue(session.state.value.didExpire)
        makeViewModel().loginWith("standard@shoplab.dev", "Secret_123")
        assertTrue(session.state.value.isLoggedIn)
        assertFalse(session.state.value.didExpire)
    }
}
