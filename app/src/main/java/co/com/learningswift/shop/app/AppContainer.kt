package co.com.learningswift.shop.app

import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.local.KeyValueStore
import co.com.learningswift.shop.data.repository.ProductRepository
import co.com.learningswift.shop.data.repository.SettingsRepository

/**
 * Crea y comparte las dependencias de la app: es el ÚNICO lugar donde se hace `new` de repositorios y del cliente.
 * Cada clase recibe lo que necesita por constructor (inyección manual, sin Hilt ni Koin).
 * Los repositorios de cada funcionalidad se añaden en su iteración.
 */
class AppContainer(baseUrl: String, store: KeyValueStore, secureStoreProvider: () -> KeyValueStore) {
    val session = SessionManager()
    private val api = ApiClient(baseUrl, session)

    val settings = SettingsRepository(store)

    /** Para datos sensibles (por ejemplo, un token persistido): cifrado. Se crea al usarlo por primera vez, no al arrancar */
    val secureStore: KeyValueStore by lazy(secureStoreProvider)
    val productRepository = ProductRepository(api)
}
