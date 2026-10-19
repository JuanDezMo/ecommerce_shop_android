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

    /** Token ya vencido de un usuario semilla (POST /api/v1/test/tokens/expired), para probar la sesión expirada */
    fun expiredToken(email: String): String {
        val connection = URI("${BASE_URL}test/tokens/expired").toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write("{\"email\": \"$email\"}".toByteArray()) }
            check(connection.responseCode == 200) { "El token expirado respondió ${connection.responseCode}" }
            val body = connection.inputStream.bufferedReader().readText()
            return Regex("\"accessToken\"\\s*:\\s*\"([^\"]+)\"").find(body)!!.groupValues[1]
        } finally {
            connection.disconnect()
        }
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
