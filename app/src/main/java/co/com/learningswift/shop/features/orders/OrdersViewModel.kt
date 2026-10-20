package co.com.learningswift.shop.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.repository.OrderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    private val _state = MutableStateFlow(OrdersUiState())
    val state: StateFlow<OrdersUiState> = _state.asStateFlow()

    /** La pantalla lo llama al aparecer y al deslizar para recargar ("Try again" lo repite). Devuelve el `Job` solo para los tests */
    fun load(): Job = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        try {
            val orders = orderRepository.orders()
            _state.update { it.copy(orders = orders, hasLoaded = true, genericError = null) }
        } catch (e: AppError) {
            _state.update { it.copy(genericError = e) }
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun dismissGenericError() = _state.update { it.copy(genericError = null) }
}
