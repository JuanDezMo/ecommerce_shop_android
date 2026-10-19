package co.com.learningswift.shop.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import co.com.learningswift.shop.data.local.AndroidKeystoreAead
import co.com.learningswift.shop.data.local.EncryptedKeyValueStore
import co.com.learningswift.shop.data.local.SharedPreferencesStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.runner.RunWith

/** Con el Android Keystore REAL del emulador o dispositivo: es lo único que la JVM no puede comprobar */
@RunWith(AndroidJUnit4::class)
class AndroidKeystoreAeadInstrumentedTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs = context.getSharedPreferences("test_secure", Context.MODE_PRIVATE)

    @Test fun aSecretSurvivesANewStoreBuiltOverTheSameKeystoreKey() {
        prefs.edit().clear().commit()
        EncryptedKeyValueStore(SharedPreferencesStore(prefs), AndroidKeystoreAead.create(context))
            .putString("token", "jwt-secret")

        // Un almacén nuevo (como tras reiniciar la app) recupera el valor con la misma clave del Keystore
        val reopened = EncryptedKeyValueStore(SharedPreferencesStore(prefs), AndroidKeystoreAead.create(context))
        assertEquals("jwt-secret", reopened.getString("token"))
    }

    @Test fun whatIsOnDiskIsNotThePlainText() {
        prefs.edit().clear().commit()
        EncryptedKeyValueStore(SharedPreferencesStore(prefs), AndroidKeystoreAead.create(context))
            .putString("token", "jwt-secret")

        assertNotEquals("jwt-secret", prefs.getString("token", null))
    }
}
