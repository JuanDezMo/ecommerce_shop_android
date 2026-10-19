package co.com.learningswift.shop.app

import co.com.learningswift.shop.core.design.SnackbarCenter
import co.com.learningswift.shop.core.network.ApiClient
import co.com.learningswift.shop.core.session.SessionManager
import co.com.learningswift.shop.data.local.KeyValueStore
import co.com.learningswift.shop.data.repository.AuthRepository
import co.com.learningswift.shop.data.repository.CartRepository
import co.com.learningswift.shop.data.repository.OrderRepository
import co.com.learningswift.shop.data.repository.ProductRepository
import co.com.learningswift.shop.data.repository.SettingsRepository
import co.com.learningswift.shop.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Crea y comparte las dependencias de la app: es el ÚNICO lugar donde se hace `new` de repositorios y del cliente.
 * Cada clase recibe lo que necesita por constructor (inyección manual, sin Hilt ni Koin).
 * Los repositorios de cada funcionalidad se añaden en su iteración.
 */
class AppContainer(
    baseUrl: String,
    store: KeyValueStore,
    secureStoreProvider: () -> KeyValueStore,
    /** Vive lo que la app: los tests pasan su `TestScope` para controlar el tiempo */
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    val session = SessionManager()
    val snackbar = SnackbarCenter(scope)

    /** Única fuente de verdad de la pestaña: la barra y cualquier pantalla la cambian escribiendo aquí */
    val selectedTab = MutableStateFlow(Tab.Catalog)
    private val api = ApiClient(baseUrl, session)

    val settings = SettingsRepository(store)

    /** Para datos sensibles (por ejemplo, un token persistido): cifrado. Se crea al usarlo por primera vez, no al arrancar */
    val secureStore: KeyValueStore by lazy(secureStoreProvider)
    val authRepository = AuthRepository(api, session)
    val cart = CartRepository(store)
    val userRepository = UserRepository(api)
    val orderRepository = OrderRepository(api)
    val productRepository = ProductRepository(api)
}
