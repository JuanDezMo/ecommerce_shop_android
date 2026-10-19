package co.com.learningswift.shop.features.productdetail

import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.CartRepository

/**
 * Qué hace el botón "Add to cart" de un producto. La fila del catálogo y el detalle comparten esta regla
 * (Comportamiento §6.1) para que nunca se comporten distinto.
 */
enum class AddToCartAction {
    Disabled,   // agotado, o el carrito ya tiene todo su stock
    CartFull,   // el carrito tiene 20 unidades: alerta "Cart is full"
    OpenSheet;  // abre la hoja para elegir la cantidad

    val isEnabled: Boolean get() = this != Disabled
}

fun addToCartAction(product: Product, cart: CartRepository): AddToCartAction = when {
    product.isSoldOut || cart.quantityOf(product.id) >= product.stock -> AddToCartAction.Disabled
    cart.isFull -> AddToCartAction.CartFull
    else -> AddToCartAction.OpenSheet
}
