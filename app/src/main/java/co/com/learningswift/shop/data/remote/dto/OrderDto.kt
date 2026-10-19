package co.com.learningswift.shop.data.remote.dto

import co.com.learningswift.shop.core.network.BigDecimalSerializer
import co.com.learningswift.shop.data.model.Order
import co.com.learningswift.shop.data.model.OrderItem
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.model.Shipping
import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: Long,
    val status: String,
    val paymentMethod: PaymentMethod,
    val items: List<OrderItemDto>,
    @Serializable(with = BigDecimalSerializer::class) val total: BigDecimal,
    val shipping: ShippingDto,
    val createdAt: String,
) {
    fun toModel() = Order(id, status, paymentMethod, items.map { it.toModel() }, total, shipping.toModel(), createdAt)
}

@Serializable
data class OrderItemDto(
    val productId: Long,
    val sku: String,
    val name: String,
    val imageKey: String,
    val quantity: Int,
    @Serializable(with = BigDecimalSerializer::class) val unitPrice: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val subtotal: BigDecimal,
) {
    fun toModel() = OrderItem(productId, sku, name, imageKey, quantity, unitPrice, subtotal)
}

@Serializable
data class ShippingDto(val address: String, val city: String, val postalCode: String, val country: String) {
    fun toModel() = Shipping(address, city, postalCode, country)
}
