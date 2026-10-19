package co.com.learningswift.shop.features.checkout

import co.com.learningswift.shop.core.network.AppJson
import co.com.learningswift.shop.data.model.PaymentMethod
import co.com.learningswift.shop.data.remote.dto.OrderDto
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderTest {
    private val json = """
        { "id": 3, "status": "CONFIRMED", "paymentMethod": "CARD",
          "items": [ { "productId": 1, "sku": "SL-HEAD-001", "name": "Wireless Headphones", "imageKey": "headphones",
                       "quantity": 2, "unitPrice": 59.99, "subtotal": 119.98 },
                     { "productId": 4, "sku": "SL-MOUS-004", "name": "Gaming Mouse", "imageKey": "mouse",
                       "quantity": 1, "unitPrice": 25.00, "subtotal": 25.00 } ],
          "total": 144.98,
          "shipping": { "address": "Calle 10 #20-30", "city": "Bogotá", "postalCode": "110111", "country": "CO" },
          "createdAt": "2026-10-14T15:04:05Z", "extraField": "ignored" }
    """.trimIndent()

    private val order = AppJson.decodeFromString(OrderDto.serializer(), json).toModel()

    @Test fun theResponseOfTheGuideIsDecoded() {
        assertEquals(3, order.id)
        assertEquals("CONFIRMED", order.status)
        assertEquals(PaymentMethod.CARD, order.paymentMethod)
        assertEquals(2, order.items.size)
        assertEquals("Bogotá", order.shipping.city)
        assertEquals("2026-10-14T15:04:05Z", order.createdAt)
    }

    @Test fun amountsKeepTheirExactValueAndTwoDecimals() {
        assertEquals(BigDecimal("144.98"), order.total)
        assertEquals(BigDecimal("59.99"), order.items[0].unitPrice)
        assertEquals(BigDecimal("119.98"), order.items[0].subtotal)
        assertEquals(BigDecimal("25.00"), order.items[1].subtotal)
    }

    @Test fun totalUnitsAddsUpTheQuantities() {
        assertEquals(3, order.totalUnits)
    }

    @Test fun theBackendMayReturnACountryTheAppDoesNotOffer() {
        val spain = AppJson.decodeFromString(OrderDto.serializer(), json.replace("\"CO\"", "\"ES\"")).toModel()
        assertEquals("ES", spain.shipping.country)
    }
}
