package co.com.learningswift.shop.features.cart

import co.com.learningswift.shop.core.errors.AppError

/** Las líneas del carrito no están aquí: salen de `CartRepository.items`. Aquí va lo que la pantalla necesita además */
data class CartUiState(
    /** Stock real por producto, de la última petición. No se guarda en el dispositivo: se queda viejo */
    val stock: Map<Long, Int> = emptyMap(),
    val genericError: AppError? = null,
)
