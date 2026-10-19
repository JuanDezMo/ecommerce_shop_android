package co.com.learningswift.shop.features.checkout

import co.com.learningswift.shop.core.network.AppJson
import co.com.learningswift.shop.data.model.CartItem
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.remote.dto.CreateOrderRequestDto
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CheckoutRequestDtoTest {
    private val items = listOf(
        CartItem(1, "Wireless Headphones", "headphones", BigDecimal("59.99"), 2),
        CartItem(4, "Gaming Mouse", "mouse", BigDecimal("25.00"), 1),
    )
    private val form = CheckoutForm(address = "  Calle 10 #20-30 ", city = " Bogotá", postalCode = "110111 ", country = Country.CO, payment = PaymentMethod.CARD)

    private val json: JsonObject = AppJson
        .parseToJsonElement(AppJson.encodeToString(CreateOrderRequestDto.serializer(), CreateOrderRequestDto.from(items, form)))
        .jsonObject

    @Test fun eachLineSendsOnlyTheProductAndTheQuantity() {
        val lines = json.getValue("items").jsonArray.map { it.jsonObject }
        assertEquals(2, lines.size)
        assertEquals(setOf("productId", "quantity"), lines[0].keys)     // nunca nombre ni precio: el backend cobra el vigente
        assertEquals(1, lines[0].getValue("productId").jsonPrimitive.int)
        assertEquals(2, lines[0].getValue("quantity").jsonPrimitive.int)
    }

    @Test fun thePaymentMethodUsesTheBackendValue() {
        assertEquals("CARD", json.getValue("paymentMethod").jsonPrimitive.content)
        val cash = AppJson.parseToJsonElement(AppJson.encodeToString(CreateOrderRequestDto.serializer(), CreateOrderRequestDto.from(items, form.copy(payment = PaymentMethod.CASH)))).jsonObject
        assertEquals("CASH", cash.getValue("paymentMethod").jsonPrimitive.content)
    }

    @Test fun theShippingIsTrimmedAndTheCountryIsACode() {
        val shipping = json.getValue("shipping").jsonObject
        assertEquals("Calle 10 #20-30", shipping.getValue("address").jsonPrimitive.content)
        assertEquals("Bogotá", shipping.getValue("city").jsonPrimitive.content)
        assertEquals("110111", shipping.getValue("postalCode").jsonPrimitive.content)
        assertEquals("CO", shipping.getValue("country").jsonPrimitive.content)
    }

    @Test fun noCardDataIsEverSent() {
        assertFalse(json.keys.any { it.contains("card", ignoreCase = true) && it != "paymentMethod" })
    }
}
