package co.com.learningswift.shop.features.orders

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.model.Order

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    /** Distingue "todavía no cargó" de "cargó y no hay órdenes" (el estado vacío solo se muestra en el segundo caso) */
    val hasLoaded: Boolean = false,
    val genericError: AppError? = null,
) {
    /** El indicador a pantalla completa es solo para la primera carga: una recarga usa el del gesto de refresco */
    val showsFullScreenLoading: Boolean get() = isLoading && !hasLoaded
}
