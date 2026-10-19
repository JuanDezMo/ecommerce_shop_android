package co.com.learningswift.shop.features.catalog

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

class CatalogViewModel(private val productRepository: ProductRepository) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    /** Carga la lista. La pantalla lo llama al aparecer y al deslizar para recargar ("Try again" lo repite). Devuelve el `Job` para los tests */
    fun load(): Job = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, loadError = false) }
        try {
            val products = productRepository.products()
            _state.update { it.copy(products = products, genericError = null, loadError = false) }
        } catch (e: AppError) {
            _state.update { it.copy(genericError = e) }
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    /** "Cancel" en la alerta: se queda la lista con su texto de error */
    fun dismissGenericError() = _state.update { it.copy(genericError = null, loadError = true) }
}
