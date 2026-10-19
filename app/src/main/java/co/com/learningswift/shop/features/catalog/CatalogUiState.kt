package co.com.learningswift.shop.features.catalog

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.data.model.Product

data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    /** Tras cancelar la alerta de un error de carga: la lista muestra "We couldn't load the products. Pull to refresh." */
    val loadError: Boolean = false,
    val genericError: AppError? = null,
) {
    /** El indicador a pantalla completa es solo para la primera carga: con datos en pantalla, una recarga usa el del gesto de refresco */
    val showsFullScreenLoading: Boolean get() = isLoading && products.isEmpty()
}
