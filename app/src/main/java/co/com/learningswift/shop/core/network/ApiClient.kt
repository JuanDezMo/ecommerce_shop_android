package co.com.learningswift.shop.core.network

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import kotlinx.coroutines.CancellationException

/**
 * Único punto que habla con el backend: arma la petición, convierte cada respuesta fallida en un AppError
 * y gestiona de forma global los 401 del token. No tiene lógica de negocio.
 *
 * `baseUrl` termina en `/` y los `path` NO empiezan por `/` (por ejemplo, `products/1`).
 */
class ApiClient(
    private val baseUrl: String,
    private val session: SessionManager,
    private val httpClient: HttpClient = defaultHttpClient(),
) {
    suspend inline fun <reified Response> get(
        path: String,
        authorized: Boolean = false,
        headers: Map<String, String> = emptyMap(),
    ): Response = decode(execute(HttpMethod.Get, path, authorized, headers) {})

    suspend inline fun <reified Request : Any, reified Response> post(
        path: String,
        body: Request,
        authorized: Boolean = false,
        headers: Map<String, String> = emptyMap(),
    ): Response = decode(
        execute(HttpMethod.Post, path, authorized, headers) {
            contentType(ContentType.Application.Json)
            setBody(body)
        },
    )

    /** Lee el cuerpo de una respuesta exitosa. Si no se puede leer, es un error desconocido */
    @PublishedApi
    internal suspend inline fun <reified Response> decode(response: HttpResponse): Response =
        try {
            response.body()
        } catch (e: CancellationException) {
            throw e                          // NUNCA tragarse la cancelación de una corrutina
        } catch (e: Exception) {
            throw AppError.Unknown
        }

    /** Envía la petición. Lanza AppError si no hay red o si el backend responde con un error */
    @PublishedApi
    internal suspend fun execute(
        method: HttpMethod,
        path: String,
        authorized: Boolean,
        headers: Map<String, String>,
        configure: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        val response = try {
            httpClient.request(baseUrl + path) {
                this.method = method
                headers.forEach { (name, value) -> header(name, value) }
                if (authorized) session.token?.let { header("Authorization", "Bearer $it") }
                configure()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw AppError.Network
        }
        if (response.status.value !in 200..299) throw mapError(response)
        return response
    }

    private suspend fun mapError(response: HttpResponse): AppError {
        val body = try {
            response.body<ErrorDto>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        val code = body?.code ?: "UNKNOWN"
        val status = response.status.value

        if (status == 401 && code == "AUTH_TOKEN_MISSING") {
            session.end()                    // bug de la app: sin aviso de sesión expirada
            return AppError.NotAuthenticated
        }
        if (status == 401 && code.startsWith("AUTH_TOKEN")) {
            session.expire()                 // EXPIRED, INVALID o MALFORMED
            return AppError.SessionExpired
        }
        // Un campo puede traer varios mensajes: se conservan todos
        val fields = body?.fieldErrors.orEmpty().groupBy({ it.field }, { it.message })
        return AppError.Api(status, code, body?.message.orEmpty(), fields)
    }

    companion object {
        fun defaultHttpClient(): HttpClient = HttpClient(OkHttp) {
            expectSuccess = false                                   // los 4xx y 5xx los mapeamos nosotros
            install(ContentNegotiation) { json(AppJson) }
            install(HttpTimeout) { requestTimeoutMillis = 15_000 }  // cubre el retraso máximo de X-Delay (10 s)
        }
    }
}
