package co.com.learningswift.shop.data.repository

import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.remote.dto.ProductDto

/** Productos del backend. Las rutas del catálogo son públicas: no llevan token */
class ProductRepository(private val api: ApiClient) {

    suspend fun products(): List<Product> =
        api.get<List<ProductDto>>("products").map { it.toModel() }

    /** Un producto inexistente lanza PRODUCT_NOT_FOUND */
    suspend fun product(id: Long): Product =
        api.get<ProductDto>("products/$id").toModel()
}
