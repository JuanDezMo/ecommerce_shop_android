package co.com.learningswift.shop.app

/**
 * Las rutas de la app, en un solo sitio. Cada pestaña es un GRAFO con su propia pila (`*_GRAPH`): al cambiar de pestaña
 * se conserva dónde estaba cada una (por ejemplo, un detalle abierto en Catalog).
 */
object Routes {
    const val CATALOG_GRAPH = "catalog_graph"
    const val CATALOG = "catalog"
    const val PRODUCT_ARG = "id"
    const val PRODUCT = "product/{$PRODUCT_ARG}"

    const val CART_GRAPH = "cart_graph"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val CONFIRMATION = "confirmation"
    const val ORDER_ARG = "id"
    const val ORDER = "order/{$ORDER_ARG}"

    const val ACCOUNT_GRAPH = "account_graph"
    const val ACCOUNT = "account"

    fun product(id: Long) = "product/$id"
    fun order(id: Long) = "order/$id"
}
