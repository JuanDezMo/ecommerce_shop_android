package co.com.learningswift.shop.features.checkout

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.model.Order

data class CheckoutUiState(
    val form: CheckoutForm = CheckoutForm(),
    /** Un campo puede traer varios mensajes (los del backend): se muestran todos, uno por línea */
    val fieldErrors: Map<CheckoutField, List<String>> = emptyMap(),
    /** Tras el primer intento, cada campo se revalida al perder el foco */
    val hasSubmitted: Boolean = false,
    val isLoading: Boolean = false,
    /** Sube cada vez que hay que desplazarse al primer error */
    val scrollTick: Int = 0,
    /** Muestra la alerta "Place order?" */
    val isConfirmingOrder: Boolean = false,
    /** Muestra la alerta "Can't place your order" con este mensaje */
    val orderErrorMessage: String? = null,
    val genericError: AppError? = null,
    /** La orden creada (201): la pantalla navega a la confirmación y esta lee los datos de aquí, sin otra consulta */
    val placedOrder: Order? = null,
) {
    val errorCount: Int get() = fieldErrors.size
    val firstErrorField: CheckoutField? get() = CheckoutField.entries.firstOrNull { it in fieldErrors }
}
