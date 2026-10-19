package co.com.learningswift.shop.support

import java.net.HttpURLConnection
import java.net.URI

/** Acceso directo al backend real para preparar las pruebas. Usa HttpURLConnection y no ApiClient: así un fallo de ApiClient no rompe el reset */
object TestBackend {
    const val HOST = "http://localhost:8080"
    const val BASE_URL = "$HOST/api/v1/"

    class NotRunning : Exception("Backend no disponible: ejecuta docker compose up en ecommerce_shop_api")

    /** Vuelve a los datos semilla (POST /api/v1/test/reset, responde 204) */
    fun reset() {
        checkHealth()
        val status = request("POST", "${BASE_URL}test/reset")
        check(status == 204) { "El reset respondió $status" }
    }

    private fun checkHealth() {
        val status = try { request("GET", "$HOST/actuator/health") } catch (e: Exception) { throw NotRunning() }
        if (status != 200) throw NotRunning()
    }

    private fun request(method: String, url: String): Int {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 3_000
        connection.readTimeout = 10_000
        return try { connection.responseCode } finally { connection.disconnect() }
    }
}
