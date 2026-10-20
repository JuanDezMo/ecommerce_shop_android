package co.com.learningswift.shop.features.orders

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.model.Order

data class OrderDetailUiState(
    val order: Order? = null,
    val isLoading: Boolean = false,
    /** 404 `ORDER_NOT_FOUND` (la orden no existe o es de otro usuario): alerta "Order not found" con "OK" */
    val notFound: Boolean = false,
    val genericError: AppError? = null,
)
