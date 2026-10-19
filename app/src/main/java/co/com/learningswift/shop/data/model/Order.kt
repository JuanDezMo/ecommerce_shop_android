package co.com.learningswift.shop.data.model

import java.math.BigDecimal

/** Una orden tal como la devuelve el backend: todo lo que se muestra de ella sale de aquí, no del carrito */
data class Order(
    val id: Long,
    val status: String,
    val paymentMethod: PaymentMethod,
    val items: List<OrderItem>,
    val total: BigDecimal,
    val shipping: Shipping,
    /** Texto ISO-8601 tal como llega. Las fechas con hora se interpretan en la iteración de "My orders" */
    val createdAt: String,
) {
    val totalUnits: Int get() = items.sumOf { it.quantity }
}

data class OrderItem(
    val productId: Long,
    val sku: String,
    val name: String,
    val imageKey: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
)

/** `country` es texto y no [Country]: el backend también devuelve `ES`, que la app no ofrece */
data class Shipping(val address: String, val city: String, val postalCode: String, val country: String)
