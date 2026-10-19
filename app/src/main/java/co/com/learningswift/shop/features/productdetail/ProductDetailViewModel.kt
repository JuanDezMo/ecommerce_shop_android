package co.com.learningswift.shop.features.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** El detalle pide su producto al backend (no reutiliza el de la lista): un producto borrado responde PRODUCT_NOT_FOUND */
class ProductDetailViewModel(private val productRepository: ProductRepository, private val productId: Long) : ViewModel() {
    private val _state = MutableStateFlow(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    /** Devuelve el `Job` solo para los tests */
    fun load(): Job = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        try {
            val product = productRepository.product(productId)
            _state.update { it.copy(product = product, genericError = null) }
        } catch (e: AppError) {
            _state.update { it.copy(genericError = e) }
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun dismissGenericError() = _state.update { it.copy(genericError = null) }
}
