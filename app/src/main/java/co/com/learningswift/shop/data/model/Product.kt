package co.com.learningswift.shop.data.model

import java.math.BigDecimal

/** Producto de la app. Sin anotaciones de red: la interfaz nunca ve un DTO */
data class Product(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val stock: Int,
    val imageKey: String,
) {
    val isSoldOut: Boolean get() = stock == 0
}
