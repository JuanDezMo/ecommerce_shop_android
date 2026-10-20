package co.com.learningswift.shop.core.network

import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral

val AppJson = Json {
    ignoreUnknownKeys = true   // el backend puede añadir campos sin romper la app
    explicitNulls = false      // no envía los campos nulos (por ejemplo, un teléfono vacío)
}

/** 59.99 se lee como el texto "59.99", nunca como Double (que daría 59.990000000000002). Montos con 2 decimales */
object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        val element = (decoder as JsonDecoder).decodeJsonElement() as JsonPrimitive
        return BigDecimal(element.content).setScale(2, RoundingMode.HALF_UP)
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        (encoder as JsonEncoder).encodeJsonElement(JsonUnquotedLiteral(value.toPlainString()))
    }
}

/**
 * Fechas con hora del backend (`createdAt`): ISO-8601 en UTC con 0 a 9 decimales. `Instant.parse` las acepta todas
 * y LANZA si el texto no es una fecha: la respuesta entera falla y `ApiClient` la trata como error desconocido.
 */
object InstantSerializer : KSerializer<java.time.Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): java.time.Instant = java.time.Instant.parse(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: java.time.Instant) = encoder.encodeString(value.toString())
}
