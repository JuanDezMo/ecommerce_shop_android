package co.com.learningswift.shop.data.local

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

/**
 * Crea el Aead de la app. Tink genera un juego de claves y lo guarda cifrado en una SharedPreferences;
 * la clave maestra que lo cifra vive en Android Keystore y nunca sale del dispositivo (no es exportable).
 * Es lo que Google recomienda ahora: uso directo de Keystore, con Tink como capa de apoyo.
 */
object AndroidKeystoreAead {
    private const val KEYSET_PREFS = "shop_keyset"
    private const val KEYSET_NAME = "shop_keyset"
    private const val MASTER_KEY_URI = "android-keystore://shop_master_key"

    fun create(context: Context): Aead {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
        return keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }
}
