package co.com.learningswift.shop.features.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.errors.FieldErrorMapper
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.Plan
import co.com.learningswift.shop.data.repository.UserRepository
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UN solo ViewModel para las dos pantallas del registro (pertenece al grafo de navegación del registro):
 * así lo escrito en el paso 1 se conserva al ir al paso 2 y volver.
 * `today` es la fecha de hoy en UTC, como la usa el backend; los tests la fijan.
 */
class RegisterViewModel(
    private val userRepository: UserRepository,
    private val today: () -> LocalDate = { LocalDate.now(ZoneOffset.UTC) },
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    // ---- campos: editar uno borra su error ----
    fun onFirstNameChange(value: String) = edit(RegisterField.FIRST_NAME) { it.copy(firstName = value) }
    fun onLastNameChange(value: String) = edit(RegisterField.LAST_NAME) { it.copy(lastName = value) }
    fun onEmailChange(value: String) = edit(RegisterField.EMAIL) { it.copy(email = value) }
    fun onPasswordChange(value: String) = edit(RegisterField.PASSWORD) { it.copy(password = value) }
    fun onPhoneChange(value: String) = edit(RegisterField.PHONE) { it.copy(phone = value) }
    fun onCountryChange(value: Country?) = edit(RegisterField.COUNTRY) { it.copy(country = value) }
    fun onPlanChange(value: Plan) = edit(RegisterField.PLAN) { it.copy(plan = value) }
    fun onExperienceChange(value: Int) = edit(RegisterField.EXPERIENCE) { it.copy(experienceLevel = value) }
    fun onNewsletterChange(value: Boolean) = _state.update { it.copy(form = it.form.copy(newsletter = value)) }
    fun onTermsChange(value: Boolean) = edit(RegisterField.TERMS) { it.copy(termsAccepted = value) }

    // ---- fecha: si el día deja de existir en el mes o el año elegidos, pasa al último día del mes ----
    fun onBirthDayChange(value: Int) = edit(RegisterField.BIRTH_DATE) { it.copy(birthDay = value) }
    fun onBirthMonthChange(value: Int) = edit(RegisterField.BIRTH_DATE) {
        it.copy(birthMonth = value, birthDay = BirthDateOptions.clampedDay(it.birthDay, it.birthYear, value))
    }
    fun onBirthYearChange(value: Int) = edit(RegisterField.BIRTH_DATE) {
        it.copy(birthYear = value, birthDay = BirthDateOptions.clampedDay(it.birthDay, value, it.birthMonth))
    }

    private fun edit(field: RegisterField, change: (RegisterForm) -> RegisterForm) = _state.update {
        it.copy(form = change(it.form), fieldErrors = it.fieldErrors - field)
    }

    /** Al salir de un campo, se revalida solo si ya hubo un primer intento */
    fun onFieldBlur(field: RegisterField) = _state.update {
        if (!it.hasSubmitted) return@update it
        val message = RegisterValidator.validate(it.form, today())[field]
        it.copy(fieldErrors = if (message == null) it.fieldErrors - field else it.fieldErrors + (field to listOf(message)))
    }

    /** "Next": valida el paso 1. Devuelve `true` si se puede avanzar. Sin errores en el paso 2: todavía no se ha visitado */
    fun continueToDetails(): Boolean {
        val errors = RegisterValidator.validate(_state.value.form, today())
            .filterKeys { it.step == RegisterStep.BASICS }
            .mapValues { listOf(it.value) }
        _state.update {
            it.copy(
                hasSubmitted = true,
                fieldErrors = errors,
                scrollTick = if (errors.isEmpty()) it.scrollTick else it.scrollTick + 1,
            )
        }
        return errors.isEmpty()
    }

    /** "Create account". Devuelve el `Job` solo para que los tests esperen el resultado; la pantalla lo ignora */
    fun submit(): Job = viewModelScope.launch {
        if (_state.value.isLoading) return@launch   // un solo envío a la vez
        val errors = RegisterValidator.validate(_state.value.form, today()).mapValues { listOf(it.value) }
        _state.update {
            it.copy(
                hasSubmitted = true,
                fieldErrors = errors,
                scrollTick = if (errors.isEmpty()) it.scrollTick else it.scrollTick + 1,
            )
        }
        if (errors.isNotEmpty()) return@launch      // errores locales: no se llama al backend

        _state.update { it.copy(isLoading = true) }
        try {
            val user = userRepository.register(_state.value.form)
            _state.update { it.copy(registeredEmail = user.email) }
        } catch (e: AppError) {
            handle(e)
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    /** Coloca un error del backend: los de negocio en su campo, VALIDATION_ERROR como red de seguridad, el resto a la alerta */
    fun handle(error: AppError) {
        val business = (error as? AppError.Api)?.let {
            when (it.code) {
                "EMAIL_ALREADY_EXISTS" -> RegisterField.EMAIL
                "UNDERAGE_USER" -> RegisterField.BIRTH_DATE
                "TERMS_NOT_ACCEPTED" -> RegisterField.TERMS
                else -> null
            }
        }
        if (business != null) {
            showFieldErrors(mapOf(business to listOf(error.userMessage)), genericError = null)
        } else if (error is AppError.Api && error.code == "VALIDATION_ERROR") {
            val mapped = FieldErrorMapper.map(error.fieldErrors, RegisterField::fromBackend)
            showFieldErrors(mapped.fields, genericError = if (mapped.hasUnmappedErrors) error else null)
        } else {
            _state.update { it.copy(genericError = error) }
        }
    }

    private fun showFieldErrors(errors: Map<RegisterField, List<String>>, genericError: AppError?) = _state.update {
        it.copy(
            fieldErrors = errors,
            genericError = genericError,
            scrollTick = if (errors.isEmpty()) it.scrollTick else it.scrollTick + 1,
        )
    }

    fun dismissGenericError() = _state.update { it.copy(genericError = null) }

    fun consumeRegisteredEmail() = _state.update { it.copy(registeredEmail = null) }
}
