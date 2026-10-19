package co.com.learningswift.shop.data.local

import com.google.crypto.tink.Aead
import java.security.GeneralSecurityException
import java.util.Base64

/**
 * Cifra cada valor ANTES de entregarlo a otro KeyValueStore (patrón decorador): quien lo usa no cambia.
 * Reemplaza a EncryptedSharedPreferences, que Google marcó como obsoleta (androidx.security:security-crypto 1.1.0-alpha07).
 *
 * - Cifrado AEAD (AES-256-GCM, con Tink): confidencialidad e integridad.
 * - La clave (`aead`) la custodia Android Keystore (ver AndroidKeystoreAead). Si el dispositivo la pierde
 *   (por ejemplo, tras restaurar una copia), el valor deja de poder leerse y se trata como si no existiera.
 * - La CLAVE del par clave-valor viaja como datos asociados: un valor cifrado no se puede copiar a otra clave.
 */
/** (java.util.Base64 y no android.util.Base64: así también corre en las pruebas de la JVM) */
class EncryptedKeyValueStore(
    private val delegate: KeyValueStore,
    private val aead: Aead,
) : KeyValueStore {

    override fun putString(key: String, value: String) {
        val encrypted = aead.encrypt(value.toByteArray(Charsets.UTF_8), key.toByteArray(Charsets.UTF_8))
        delegate.putString(key, Base64.getEncoder().encodeToString(encrypted))
    }

    override fun getString(key: String): String? {
        val stored = delegate.getString(key) ?: return null
        return try {
            val decrypted = aead.decrypt(Base64.getDecoder().decode(stored), key.toByteArray(Charsets.UTF_8))
            String(decrypted, Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            null            // dato alterado o clave perdida: como si no existiera
        } catch (e: IllegalArgumentException) {
            null            // no era Base64 válido
        }
    }
}
