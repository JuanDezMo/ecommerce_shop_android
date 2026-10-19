package co.com.learningswift.shop.features.register

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.repository.UserRepository
import co.com.learningswift.shop.support.MainDispatcherRule
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule

/** Sin red: el ApiClient apunta a un puerto donde no escucha nada, así cualquier llamada indebida se nota (error de red) */
class RegisterViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val today = LocalDate.of(2026, 10, 11)
    private fun makeViewModel() = RegisterViewModel(
        UserRepository(ApiClient("http://localhost:9/api/v1/", SessionManager())),
        today = { today },
    )

    private val form get() = viewModel.state.value.form
    private val state get() = viewModel.state.value
    private val viewModel = makeViewModel()

    private fun fillBasics() = viewModel.run {
        onFirstNameChange("Alexandria"); onLastNameChange("Montgomery"); onEmailChange("ana@example.com")
        onPasswordChange("Secret_123"); onPhoneChange("+5733001234567")
    }

    private fun apiError(status: Int, code: String, fields: Map<String, List<String>> = emptyMap()) =
        AppError.Api(status, code, "technical message", fields)

    @Test fun initialValues() {
        assertEquals(1, form.birthDay); assertEquals(1, form.birthMonth); assertEquals(2000, form.birthYear)
        assertNull(form.country); assertEquals(3, form.experienceLevel)
        assertFalse(form.newsletter); assertFalse(form.termsAccepted)
        assertTrue(state.fieldErrors.isEmpty())
    }

    @Test fun changingTheMonthToFebruaryDropsDay31() {
        viewModel.onBirthDayChange(31)
        viewModel.onBirthMonthChange(2)
        assertEquals(29, form.birthDay)                // 2000 fue bisiesto
        viewModel.onBirthDayChange(31)
        viewModel.onBirthYearChange(2001)
        assertEquals(28, form.birthDay)
    }

    @Test fun changingTheYearDropsFebruary29() {
        viewModel.onBirthMonthChange(2)
        viewModel.onBirthDayChange(29)
        viewModel.onBirthYearChange(2001)
        assertEquals(28, form.birthDay)
    }

    @Test fun continueWithAnEmptyFirstStepStaysAndMarksTheFiveFields() {
        assertFalse(viewModel.continueToDetails())
        assertEquals(
            setOf(RegisterField.FIRST_NAME, RegisterField.LAST_NAME, RegisterField.EMAIL, RegisterField.PASSWORD, RegisterField.PHONE),
            state.fieldErrors.keys,
        )
        assertEquals(5, state.errorCount(RegisterStep.BASICS))
        assertEquals(0, state.errorCount(RegisterStep.DETAILS))   // el paso 2 todavía no se ha visitado
        assertEquals(1, state.scrollTick)
    }

    @Test fun continueWithAValidFirstStepAdvancesWithoutErrors() {
        fillBasics()
        assertTrue(viewModel.continueToDetails())
        assertTrue(state.fieldErrors.isEmpty())
    }

    @Test fun editingAFieldClearsItsError() {
        viewModel.continueToDetails()
        viewModel.onEmailChange("a")
        assertNull(state.fieldErrors[RegisterField.EMAIL])
        assertNotNull(state.fieldErrors[RegisterField.FIRST_NAME])
    }

    @Test fun changingTheDateClearsTheDateError() = runTest {
        fillBasics()
        viewModel.onBirthYearChange(2020)
        viewModel.submit().join()
        assertNotNull(state.fieldErrors[RegisterField.BIRTH_DATE])
        viewModel.onBirthDayChange(2)
        assertNull(state.fieldErrors[RegisterField.BIRTH_DATE])
    }

    @Test fun leavingAFieldRevalidatesItOnlyAfterTheFirstAttempt() {
        viewModel.onFieldBlur(RegisterField.EMAIL)
        assertNull(state.fieldErrors[RegisterField.EMAIL])            // sin intento previo: sin error
        viewModel.continueToDetails()
        viewModel.onEmailChange("ana@example.com")
        viewModel.onEmailChange("")
        viewModel.onFieldBlur(RegisterField.EMAIL)
        assertEquals(listOf("Enter your email."), state.fieldErrors[RegisterField.EMAIL])
    }

    @Test fun submittingWithLocalErrorsDoesNotCallTheBackendAndPointsToTheStepOfTheFirstError() = runTest {
        viewModel.submit().join()
        assertNull(state.genericError)                  // si hubiera llamado al backend sería un error de red
        assertFalse(state.isLoading)
        assertEquals(RegisterStep.BASICS, state.stepOfFirstError)
        fillBasics()
        viewModel.submit().join()
        assertNull(state.genericError)
        assertEquals(setOf(RegisterField.COUNTRY, RegisterField.TERMS), state.fieldErrors.keys)
        assertEquals(RegisterStep.DETAILS, state.stepOfFirstError)
        assertEquals(2, state.errorCount(RegisterStep.DETAILS))
    }

    @Test fun emailAlreadyExistsGoesUnderTheEmailAndBackToTheFirstStep() {
        viewModel.handle(apiError(409, "EMAIL_ALREADY_EXISTS"))
        assertEquals(listOf("An account with this email already exists."), state.fieldErrors[RegisterField.EMAIL])
        assertEquals(RegisterStep.BASICS, state.stepOfFirstError)
        assertNull(state.genericError)
    }

    @Test fun underageAndTermsGoUnderTheirFields() {
        viewModel.handle(apiError(422, "UNDERAGE_USER"))
        assertEquals(listOf("You must be at least 18 years old to register."), state.fieldErrors[RegisterField.BIRTH_DATE])
        viewModel.handle(apiError(422, "TERMS_NOT_ACCEPTED"))
        assertEquals(listOf("You must accept the terms and conditions."), state.fieldErrors[RegisterField.TERMS])
        assertEquals(RegisterStep.DETAILS, state.stepOfFirstError)
    }

    @Test fun validationErrorFieldsAreShownAsTheyArrive() {
        viewModel.handle(apiError(400, "VALIDATION_ERROR", mapOf("experienceLevel" to listOf("must be between 1 and 5"))))
        assertEquals(listOf("must be between 1 and 5"), state.fieldErrors[RegisterField.EXPERIENCE])
        assertNull(state.genericError)
    }

    @Test fun validationErrorWithAnUnknownOrNoFieldOpensTheGenericAlert() {
        viewModel.handle(apiError(400, "VALIDATION_ERROR", mapOf("nickname" to listOf("bad"))))
        assertNotNull(state.genericError)
        assertTrue(state.fieldErrors.isEmpty())
        viewModel.dismissGenericError()
        viewModel.handle(apiError(400, "VALIDATION_ERROR"))
        assertNotNull(state.genericError)
    }

    @Test fun errorsWithAndWithoutFieldShowBoth() {
        viewModel.handle(apiError(400, "VALIDATION_ERROR", mapOf("email" to listOf("bad"), "nickname" to listOf("bad"))))
        assertEquals(listOf("bad"), state.fieldErrors[RegisterField.EMAIL])
        assertNotNull(state.genericError)
    }

    @Test fun anythingElseOpensTheGenericAlert() {
        viewModel.handle(AppError.Network)
        assertEquals(AppError.Network, state.genericError)
        viewModel.dismissGenericError()
        assertNull(state.genericError)
    }

    @Test fun countryAndTermsAreStoredInTheForm() {
        viewModel.onCountryChange(Country.CO)
        viewModel.onTermsChange(true)
        assertEquals(Country.CO, form.country)
        assertTrue(form.termsAccepted)
    }
}
