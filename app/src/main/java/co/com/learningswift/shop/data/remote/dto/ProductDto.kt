package co.com.learningswift.shop.data.remote.dto

import co.com.learningswift.shop.core.network.BigDecimalSerializer
import co.com.learningswift.shop.data.model.Product
import java.math.BigDecimal
import kotlinx.serialization.Serializable

/** Producto tal como lo devuelve el backend (`GET products` y `GET products/{id}`) */
@Serializable
data class ProductDto(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class) val price: BigDecimal,
    val stock: Int,
    val imageKey: String,
) {
    fun toModel() = Product(id, sku, name, description, price, stock, imageKey)
}
