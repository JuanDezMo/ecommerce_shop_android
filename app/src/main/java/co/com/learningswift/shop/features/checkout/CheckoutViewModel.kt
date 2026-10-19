package co.com.learningswift.shop.features.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.errors.FieldErrorMapper
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.OrderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * El checkout. REGLAS que no se rompen:
 * - El carrito solo se modifica con el 201 (`cart.clear()` en [placeOrder]); cualquier error lo deja intacto.
 * - "Try again" de la alerta genérica NO reenvía: reabre "Place order?" ([placeOrderTapped]). El backend no es idempotente:
 *   si la respuesta se perdió por la red, la orden pudo crearse y reenviar sin preguntar podría duplicarla.
 */
class CheckoutViewModel(private val cart: CartRepository, private val orderRepository: OrderRepository) : ViewModel() {
    private val _state = MutableStateFlow(CheckoutUiState())
    val state: StateFlow<CheckoutUiState> = _state.asStateFlow()

    // ---- campos: editar uno borra su error ----
    fun onAddressChange(value: String) = edit(CheckoutField.ADDRESS) { it.copy(address = value) }
    fun onCityChange(value: String) = edit(CheckoutField.CITY) { it.copy(city = value) }
    fun onPostalCodeChange(value: String) = edit(CheckoutField.POSTAL_CODE) { it.copy(postalCode = value) }
    fun onCountryChange(value: Country?) = edit(CheckoutField.COUNTRY) { it.copy(country = value) }
    fun onPaymentChange(value: PaymentMethod) = edit(CheckoutField.PAYMENT) { it.copy(payment = value) }

    private fun edit(field: CheckoutField, change: (CheckoutForm) -> CheckoutForm) = _state.update {
        it.copy(form = change(it.form), fieldErrors = it.fieldErrors - field)
    }

    /** Al salir de un campo, se revalida solo si ya hubo un primer intento */
    fun onFieldBlur(field: CheckoutField) = _state.update {
        if (!it.hasSubmitted) return@update it
        val message = CheckoutValidator.validate(it.form)[field]
        it.copy(fieldErrors = if (message == null) it.fieldErrors - field else it.fieldErrors + (field to listOf(message)))
    }

    /** "Place order": valida; sin errores, abre "Place order?". Con errores no abre nada ni llama al backend */
    fun placeOrderTapped() {
        val errors = CheckoutValidator.validate(_state.value.form).mapValues { listOf(it.value) }
        _state.update {
            it.copy(
                hasSubmitted = true,
                fieldErrors = errors,
                isConfirmingOrder = errors.isEmpty(),
                scrollTick = if (errors.isEmpty()) it.scrollTick else it.scrollTick + 1,
            )
        }
    }

    /** "Cancel" en "Place order?": vuelve al formulario con lo escrito, sin enviar nada */
    fun dismissPlaceOrder() = _state.update { it.copy(isConfirmingOrder = false) }

    /** "Place order" en la alerta: envía la orden. Un solo envío a la vez. Devuelve el `Job` solo para los tests */
    fun placeOrder(): Job = viewModelScope.launch {
        if (_state.value.isLoading) return@launch
        _state.update { it.copy(isConfirmingOrder = false, isLoading = true) }
        try {
            val order = orderRepository.createOrder(cart.items.value, _state.value.form)
            cart.clear()                                  // el ÚNICO punto donde se modifica el carrito
            _state.update { it.copy(placedOrder = order) }
        } catch (e: AppError) {
            handle(e)
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    /** Coloca un error del backend: de negocio → "Can't place your order"; VALIDATION_ERROR → su campo; el resto → alerta genérica */
    fun handle(error: AppError) {
        val api = error as? AppError.Api
        when {
            api != null && api.code in BUSINESS_CODES -> _state.update { it.copy(orderErrorMessage = error.userMessage) }
            api != null && api.code == "VALIDATION_ERROR" -> {
                val mapped = FieldErrorMapper.map(api.fieldErrors, CheckoutField::fromBackend)
                _state.update {
                    it.copy(
                        fieldErrors = mapped.fields,
                        orderErrorMessage = if (mapped.hasUnmappedErrors) AlertText.OrderError.CART_MESSAGE else null,
                        scrollTick = if (mapped.fields.isEmpty()) it.scrollTick else it.scrollTick + 1,
                    )
                }
            }
            else -> _state.update { it.copy(genericError = error) }
        }
    }

    fun dismissOrderError() = _state.update { it.copy(orderErrorMessage = null) }

    fun dismissGenericError() = _state.update { it.copy(genericError = null) }

    private companion object {
        val BUSINESS_CODES = setOf("INSUFFICIENT_STOCK", "PRODUCT_NOT_FOUND", "EMPTY_ORDER", "DUPLICATE_ORDER_ITEM")
    }
}
