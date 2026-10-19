package co.com.learningswift.shop

import android.app.Application
import co.com.learningswift.shop.app.AppContainer
import co.com.learningswift.shop.core.network.AppConfig
import co.com.learningswift.shop.data.local.AndroidKeystoreAead
import co.com.learningswift.shop.data.local.EncryptedKeyValueStore
import co.com.learningswift.shop.data.local.SharedPreferencesStore

class ShopApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val preferences = getSharedPreferences("shop", MODE_PRIVATE)
        container = AppContainer(
            baseUrl = AppConfig.baseUrl,
            store = SharedPreferencesStore(preferences),
            secureStoreProvider = {
                EncryptedKeyValueStore(
                    SharedPreferencesStore(getSharedPreferences("shop_secure", MODE_PRIVATE)),
                    AndroidKeystoreAead.create(this),
                )
            },
        )
    }
}
