package co.com.learningswift.shop.features.checkout

import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.PaymentMethod

/** Lo que el usuario ha escrito o elegido. Los productos no están aquí: salen del carrito */
data class CheckoutForm(
    val address: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: Country? = null,       // null = "Select"
    val payment: PaymentMethod = PaymentMethod.CARD,
)
