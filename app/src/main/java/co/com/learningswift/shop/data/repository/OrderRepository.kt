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

    /** Las órdenes del usuario, la más reciente primero (el orden lo da el backend) */
    suspend fun orders(): List<Order> = api.get<List<OrderDto>>("orders", authorized = true).map { it.toModel() }

    /** Una orden. 404 `ORDER_NOT_FOUND` si no existe o es de otro usuario */
    suspend fun order(id: Long): Order = api.get<OrderDto>("orders/$id", authorized = true).toModel()
}
