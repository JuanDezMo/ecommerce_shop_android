package co.com.learningswift.shop.features.register

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.repository.AuthRepository
import co.com.learningswift.shop.data.repository.UserRepository
import co.com.learningswift.shop.support.MainDispatcherRule
import co.com.learningswift.shop.support.TestBackend
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule

/** Requieren el backend levantado (docker compose up en ecommerce_shop_api, perfil testing). Sin mocks: HTTP real */
class RegistrationIntegrationTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val session = SessionManager()
    private val api = ApiClient(TestBackend.BASE_URL, session)
    private val users = UserRepository(api)
    private val validForm = RegisterForm(
        firstName = "Alexandria", lastName = "Montgomery", email = "ana@example.com",
        password = "Secret_123", phone = "+5733001234567", country = Country.CO, termsAccepted = true,
    )

    @BeforeTest fun resetBackend() = TestBackend.reset()

    private suspend fun apiError(form: RegisterForm): AppError.Api =
        assertFailsWith<AppError.Api> { users.register(form) }

    // ---- repositorio ----
    @Test fun aValidFormCreatesTheAccountAndThenTheUserCanLogIn() = runTest {
        val user = users.register(validForm)
        assertEquals("ACTIVE", user.status)
        assertEquals("ana@example.com", user.email)
        AuthRepository(api, session).login("ana@example.com", "Secret_123")
        assertTrue(session.state.value.isLoggedIn)
    }

    @Test fun anExistingEmailIsRejectedEvenWithOtherCase() = runTest {
        for (email in listOf("standard@shoplab.dev", "Standard@ShopLab.dev")) {
            val error = apiError(validForm.copy(email = email))
            assertEquals(409, error.status)
            assertEquals("EMAIL_ALREADY_EXISTS", error.code)
        }
    }

    @Test fun aMinorIsRejected() = runTest {
        val error = apiError(validForm.copy(birthYear = 2020))
        assertEquals(422, error.status)
        assertEquals("UNDERAGE_USER", error.code)
    }

    @Test fun termsNotAcceptedAreRejected() = runTest {
        val error = apiError(validForm.copy(termsAccepted = false))
        assertEquals(422, error.status)
        assertEquals("TERMS_NOT_ACCEPTED", error.code)
    }

    @Test fun aBadPhoneSentDirectlyIsAValidationError() = runTest {
        val error = apiError(validForm.copy(phone = "300-123"))
        assertEquals(400, error.status)
        assertEquals("VALIDATION_ERROR", error.code)
        assertNotNull(error.fieldErrors["phone"])
    }

    // ---- ViewModel contra el backend real ----
    private fun makeViewModel(baseUrl: String = TestBackend.BASE_URL) =
        RegisterViewModel(UserRepository(ApiClient(baseUrl, session)), today = { LocalDate.of(2026, 10, 11) })

    private fun RegisterViewModel.fill(form: RegisterForm) {
        onFirstNameChange(form.firstName); onLastNameChange(form.lastName); onEmailChange(form.email)
        onPasswordChange(form.password); onPhoneChange(form.phone); onCountryChange(form.country)
        onTermsChange(form.termsAccepted)
    }

    @Test fun theViewModelRegistersWithATrimmedLowercaseEmailAndThenTheUserCanLogIn() = runTest {
        val viewModel = makeViewModel()
        viewModel.fill(validForm.copy(email = "  Ana@Example.COM "))
        viewModel.submit().join()
        assertEquals("ana@example.com", viewModel.state.value.registeredEmail)
        assertTrue(viewModel.state.value.fieldErrors.isEmpty())
        AuthRepository(api, session).login("ana@example.com", "Secret_123")
        assertTrue(session.state.value.isLoggedIn)
    }

    @Test fun anExistingEmailShowsTheFieldErrorWithoutAlertAndPointsToTheFirstStep() = runTest {
        val viewModel = makeViewModel()
        viewModel.fill(validForm.copy(email = "standard@shoplab.dev"))
        viewModel.submit().join()
        val state = viewModel.state.value
        assertEquals(listOf("An account with this email already exists."), state.fieldErrors[RegisterField.EMAIL])
        assertEquals(1, state.errorCount(RegisterStep.BASICS))
        assertEquals(RegisterStep.BASICS, state.stepOfFirstError)
        assertNull(state.genericError)
        assertNull(state.registeredEmail)
    }

    @Test fun aFormWithLocalErrorsDoesNotCreateTheAccount() = runTest {
        val viewModel = makeViewModel()
        viewModel.fill(validForm.copy(termsAccepted = false))
        viewModel.submit().join()
        assertNull(viewModel.state.value.registeredEmail)
        viewModel.onTermsChange(true)
        viewModel.submit().join()                       // el mismo email se registra: la cuenta no se había creado
        assertEquals("ana@example.com", viewModel.state.value.registeredEmail)
    }

    @Test fun aForcedErrorOpensTheGenericAlertAndCreatesNothing() = runTest {
        val viewModel = makeViewModel()
        viewModel.fill(validForm.copy(email = "force503@shoplab.dev"))
        viewModel.submit().join()
        assertEquals("Something went wrong on our side. Please try again later.", viewModel.state.value.genericError?.userMessage)
        assertNull(viewModel.state.value.registeredEmail)
        viewModel.dismissGenericError()
        viewModel.submit().join()                       // "Try again": reenvía el mismo formulario
        assertNotNull(viewModel.state.value.genericError)
    }

    @Test fun anUnreachableBackendOpensTheGenericAlertAndKeepsTheData() = runTest {
        val viewModel = makeViewModel(baseUrl = "http://localhost:9/api/v1/")
        viewModel.fill(validForm)
        viewModel.submit().join()
        assertEquals(AppError.Network, viewModel.state.value.genericError)
        viewModel.dismissGenericError()
        assertEquals("Alexandria", viewModel.state.value.form.firstName)
    }

    @Test fun twoQuickSubmitsCreateOnlyOneAccount() = runTest {
        val viewModel = makeViewModel()
        viewModel.fill(validForm)
        val first = viewModel.submit()
        val second = viewModel.submit()
        first.join(); second.join()
        assertEquals("ana@example.com", viewModel.state.value.registeredEmail)
        assertTrue(viewModel.state.value.fieldErrors.isEmpty())   // el segundo no llegó al backend: no hay "email repetido"
        assertNull(viewModel.state.value.genericError)
    }
}
