package co.com.learningswift.shop.data.model

import co.com.learningswift.shop.core.network.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.serialization.Serializable

/** Una línea del carrito. No guarda el stock: un stock guardado se queda viejo (el stock real llega del backend) */
@Serializable
data class CartItem(
    val productId: Long,
    val name: String,
    val imageKey: String,
    @Serializable(with = BigDecimalSerializer::class) val unitPrice: BigDecimal,
    val quantity: Int,
)
