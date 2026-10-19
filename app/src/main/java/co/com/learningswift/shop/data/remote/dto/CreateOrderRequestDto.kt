package co.com.learningswift.shop.data.remote.dto

import co.com.learningswift.shop.data.model.CartItem
import co.com.learningswift.shop.features.checkout.CheckoutForm
import kotlinx.serialization.Serializable

/** Cuerpo de `POST orders`. De cada línea solo van el producto y la cantidad: el backend cobra el precio vigente */
@Serializable
data class CreateOrderRequestDto(val items: List<LineDto>, val paymentMethod: String, val shipping: ShippingDto) {
    @Serializable
    data class LineDto(val productId: Long, val quantity: Int)

    companion object {
        fun from(items: List<CartItem>, form: CheckoutForm) = CreateOrderRequestDto(
            items = items.map { LineDto(it.productId, it.quantity) },
            paymentMethod = form.payment.name,
            shipping = ShippingDto(
                address = form.address.trim(),
                city = form.city.trim(),
                postalCode = form.postalCode.trim(),
                country = form.country?.code.orEmpty(),
            ),
        )
    }
}
