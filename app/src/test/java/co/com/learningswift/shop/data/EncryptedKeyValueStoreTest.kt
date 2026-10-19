package co.com.learningswift.shop.data

import co.com.learningswift.shop.data.local.EncryptedKeyValueStore
import co.com.learningswift.shop.data.local.InMemoryStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/** Cifrado real de Tink (AES-256-GCM) con un juego de claves en memoria: en la JVM no existe Android Keystore, pero el cifrado es el mismo */
class EncryptedKeyValueStoreTest {
    private val raw = InMemoryStore()

    private fun newAead(): Aead {
        AeadConfig.register()
        return KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM")).getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private val aead = newAead()
    private val store = EncryptedKeyValueStore(raw, aead)

    @BeforeTest fun registerTink() = AeadConfig.register()

    @Test fun aValueIsRecoveredAfterSavingIt() {
        store.putString("token", "eyJhbGciOiJIUzI1NiJ9.payload.signature")
        assertEquals("eyJhbGciOiJIUzI1NiJ9.payload.signature", store.getString("token"))
    }

    @Test fun whatIsWrittenToTheDelegateIsNotThePlainText() {
        store.putString("token", "secret-token")
        val stored = raw.getString("token")
        assertNotEquals("secret-token", stored)
        assertEquals(false, stored!!.contains("secret-token"))
    }

    @Test fun savingTheSameValueTwiceGivesDifferentCiphertexts() {
        store.putString("a", "same")
        val first = raw.getString("a")
        store.putString("a", "same")
        assertNotEquals(first, raw.getString("a"))   // IV aleatorio en cada cifrado
    }

    @Test fun aMissingKeyIsNull() {
        assertNull(store.getString("nothing"))
    }

    @Test fun aValueCopiedToAnotherKeyCannotBeRead() {
        store.putString("token", "secret")
        raw.putString("other", raw.getString("token")!!)
        assertNull(store.getString("other"))         // la clave viaja como datos asociados
    }

    @Test fun aTamperedValueIsNull() {
        store.putString("token", "secret")
        val stored = raw.getString("token")!!
        raw.putString("token", stored.dropLast(4) + "AAAA")
        assertNull(store.getString("token"))
    }

    @Test fun aLostKeyMakesTheValueUnreadable() {
        store.putString("token", "secret")
        val withAnotherKey = EncryptedKeyValueStore(raw, newAead())   // como tras perder la clave del Keystore
        assertNull(withAnotherKey.getString("token"))
    }

    @Test fun garbageInTheDelegateIsNull() {
        raw.putString("token", "***not base64***")
        assertNull(store.getString("token"))
    }
}
