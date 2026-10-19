package co.com.learningswift.shop.data.local

import android.content.SharedPreferences

/**
 * Almacenamiento local de pares clave-valor (carrito y tema). Es la ÚNICA abstracción del proyecto, y existe
 * porque tiene dos implementaciones reales: SharedPreferences en el dispositivo y una en memoria para las
 * pruebas de la JVM, donde Android no existe. Regla del proyecto: una interfaz solo con dos implementaciones hoy.
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

class SharedPreferencesStore(private val preferences: SharedPreferences) : KeyValueStore {
    override fun getString(key: String): String? = preferences.getString(key, null)
    override fun putString(key: String, value: String) = preferences.edit().putString(key, value).apply()
}

class InMemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) { values[key] = value }
}
