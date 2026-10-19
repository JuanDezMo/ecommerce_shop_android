package co.com.learningswift.shop.data.repository

import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.data.model.CartItem
import co.com.learningswift.shop.data.model.Order
import co.com.learningswift.shop.data.remote.dto.CreateOrderRequestDto
import co.com.learningswift.shop.data.remote.dto.OrderDto
import co.com.learningswift.shop.features.checkout.CheckoutForm

class OrderRepository(private val api: ApiClient) {
    /**
     * Crea la orden (`POST orders`, con token). No toca el carrito: solo el 201 lo vacía y eso lo decide el ViewModel.
     * Si el backend rechaza la orden, no descuenta stock de ningún producto.
     */
    suspend fun createOrder(items: List<CartItem>, form: CheckoutForm): Order {
        val dto: OrderDto = api.post("orders", CreateOrderRequestDto.from(items, form), authorized = true)
        return dto.toModel()
    }
}
