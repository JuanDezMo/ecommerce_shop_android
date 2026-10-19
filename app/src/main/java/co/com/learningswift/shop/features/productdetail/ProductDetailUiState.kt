package co.com.learningswift.shop.features.productdetail

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.model.Product

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val genericError: AppError? = null,
)
