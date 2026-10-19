package co.com.learningswift.shop.features.checkout

/**
 * Los campos del checkout, EN EL ORDEN del formulario (el primero con error es el primero de esta lista).
 * Un solo sitio conoce los tres nombres de cada campo: el de la app, el del catálogo de IDs y el del backend.
 */
enum class CheckoutField(val tagSuffix: String, val backendName: String) {
    ADDRESS("address", "shipping.address"),
    CITY("city", "shipping.city"),
    POSTAL_CODE("postalcode", "shipping.postalCode"),
    COUNTRY("country", "shipping.country"),
    PAYMENT("payment", "paymentMethod");

    companion object {
        /** Los errores de `items`, `items[n]…` o `shipping` (sin campo) no tienen campo en el formulario: devuelven `null` */
        fun fromBackend(name: String): CheckoutField? = entries.firstOrNull { it.backendName == name }
    }
}
