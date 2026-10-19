package co.com.learningswift.shop.core.network

import co.com.learningswift.shop.BuildConfig

object AppConfig {
    /** Debug: el backend de la Mac visto desde el emulador. Release: la URL de producción (pendiente) */
    val baseUrl: String get() = BuildConfig.API_BASE_URL
}
