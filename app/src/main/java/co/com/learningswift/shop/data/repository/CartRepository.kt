package co.com.learningswift.shop.data.repository

import co.com.learningswift.shop.core.network.AppJson
import co.com.learningswift.shop.data.local.KeyValueStore
import co.com.learningswift.shop.data.model.CartItem
import co.com.learningswift.shop.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer

/**
 * Carrito guardado en el dispositivo (el backend no tiene carrito). Reglas de Comportamiento §7.1:
 * una línea por producto, máximo 20 unidades en total y nunca más unidades que el stock.
 */
class CartRepository(private val store: KeyValueStore) {
    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    val totalQuantity: Int get() = _items.value.sumOf { it.quantity }
    val isFull: Boolean get() = totalQuantity >= MAX_TOTAL_QUANTITY

    fun quantityOf(productId: Long): Int = _items.value.firstOrNull { it.productId == productId }?.quantity ?: 0

    /** Cuántas unidades más de este producto caben: lo que queda de stock y lo que falta para llegar a 20 */
    fun maxAddable(product: Product): Int {
        val byStock = product.stock - quantityOf(product.id)
        val byCart = MAX_TOTAL_QUANTITY - totalQuantity
        return maxOf(0, minOf(byStock, byCart))
    }

    /** Un producto aparece una sola vez: añadir más unidades suma a su línea (evita DUPLICATE_ORDER_ITEM) */
    fun add(product: Product, quantity: Int) {
        val toAdd = minOf(quantity, maxAddable(product))
        if (toAdd <= 0) return

        val current = _items.value
        _items.value =
            if (current.any { it.productId == product.id }) {
                current.map { if (it.productId == product.id) it.copy(quantity = it.quantity + toAdd) else it }
            } else {
                current + CartItem(product.id, product.name, product.imageKey, product.price, toAdd)
            }
        save()
    }

    /**
     * Cambia la cantidad de una línea: nunca menos de 1, ni más que el stock, ni más de lo que deja el tope de 20.
     * El stock llega de fuera porque el carrito no lo guarda (se queda viejo): lo trae la última petición de productos.
     */
    fun setQuantity(quantity: Int, productId: Long, stock: Int) {
        val current = quantityOf(productId)
        if (current == 0) return
        val allowed = minOf(stock, current + MAX_TOTAL_QUANTITY - totalQuantity)
        val newQuantity = maxOf(1, minOf(quantity, allowed))
        if (newQuantity == current) return

        _items.value = _items.value.map { if (it.productId == productId) it.copy(quantity = newQuantity) else it }
        save()
    }

    fun remove(productId: Long) {
        _items.value = _items.value.filterNot { it.productId == productId }
        save()
    }

    fun clear() {
        _items.value = emptyList()
        save()
    }

    /**
     * Ajusta el carrito al stock real: baja las cantidades y quita los productos agotados o que ya no existen.
     * Devuelve `true` si cambió algo (la pantalla muestra entonces el snackbar de Comportamiento §7.2).
     */
    fun reconcile(products: List<Product>): Boolean {
        val adjusted = _items.value.mapNotNull { item ->
            val product = products.firstOrNull { it.id == item.productId }
            if (product == null || product.stock <= 0) null else item.copy(quantity = minOf(item.quantity, product.stock))
        }
        if (adjusted == _items.value) return false

        _items.value = adjusted
        save()
        return true
    }

    private fun save() {
        store.putString(KEY, AppJson.encodeToString(ListSerializer(CartItem.serializer()), _items.value))
    }

    /** Un JSON ilegible (por ejemplo, de una versión anterior) da un carrito vacío en lugar de cerrar la app */
    private fun load(): List<CartItem> {
        val json = store.getString(KEY) ?: return emptyList()
        return try {
            AppJson.decodeFromString(ListSerializer(CartItem.serializer()), json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        const val MAX_TOTAL_QUANTITY = 20
        private const val KEY = "cart.items"
    }
}
