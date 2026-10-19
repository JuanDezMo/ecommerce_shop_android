package co.com.learningswift.shop.features.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.learningswift.shop.core.design.SnackbarCenter
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.model.CartItem
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartViewModel(
    private val cart: CartRepository,
    private val productRepository: ProductRepository,
    val snackbar: SnackbarCenter,
) : ViewModel() {
    private val _state = MutableStateFlow(CartUiState())
    val state: StateFlow<CartUiState> = _state.asStateFlow()

    /**
     * Ajusta el carrito al stock real. La pantalla lo llama cada vez que aparece ("Try again" lo repite).
     * Si la petición falla, el ajuste no se llama: el carrito no se toca. Devuelve el `Job` solo para los tests.
     */
    fun refresh(): Job = viewModelScope.launch {
        try {
            val products = productRepository.products()
            _state.update { it.copy(stock = products.associate { p -> p.id to p.stock }, genericError = null) }
            if (cart.reconcile(products)) snackbar.show(AlertText.Snackbar.CART_UPDATED)
        } catch (e: AppError) {
            _state.update { it.copy(genericError = e) }
        }
    }

    /**
     * Hasta dónde puede subir el selector de una línea: el menor entre el stock y lo que ya tiene más lo que falta para 20.
     * Sin stock conocido (antes de la primera respuesta) no deja subir.
     */
    fun maxQuantity(item: CartItem): Int {
        val stock = _state.value.stock[item.productId] ?: item.quantity
        return minOf(stock, item.quantity + CartRepository.MAX_TOTAL_QUANTITY - cart.totalQuantity)
    }

    fun setQuantity(productId: Long, quantity: Int) {
        val stock = _state.value.stock[productId] ?: cart.quantityOf(productId)
        cart.setQuantity(quantity, productId, stock)
    }

    fun remove(productId: Long) = cart.remove(productId)

    fun clear() = cart.clear()

    fun dismissGenericError() = _state.update { it.copy(genericError = null) }
}
