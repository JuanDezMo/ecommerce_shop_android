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

class OrderDetailViewModel(private val orderRepository: OrderRepository, private val orderId: Long) : ViewModel() {
    private val _state = MutableStateFlow(OrderDetailUiState())
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()

    /** Devuelve el `Job` solo para los tests */
    fun load(): Job = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        try {
            val order = orderRepository.order(orderId)
            _state.update { it.copy(order = order, notFound = false, genericError = null) }
        } catch (e: AppError) {
            if (e is AppError.Api && e.code == "ORDER_NOT_FOUND") _state.update { it.copy(notFound = true) }
            else _state.update { it.copy(genericError = e) }
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun dismissNotFound() = _state.update { it.copy(notFound = false) }

    fun dismissGenericError() = _state.update { it.copy(genericError = null) }
}
