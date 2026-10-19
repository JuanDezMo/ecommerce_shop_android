package co.com.learningswift.shop.core

import co.com.learningswift.shop.core.formatting.usd
import co.com.learningswift.shop.core.network.AppJson
import co.com.learningswift.shop.data.remote.dto.ProductDto
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonAndMoneyTest {
    private fun decode(json: String) = AppJson.decodeFromString<ProductDto>(json)

    @Test fun thePriceIsExactlyWhatTheBackendSent() {
        val product = decode("""{"id":1,"sku":"A","name":"A","description":"A","price":59.99,"stock":10,"imageKey":"headphones"}""")
        assertEquals(BigDecimal("59.99"), product.price)
    }

    @Test fun integerPricesKeepTwoDecimals() {
        val product = decode("""{"id":4,"sku":"A","name":"A","description":"A","price":25.0,"stock":20,"imageKey":"mouse"}""")
        assertEquals(BigDecimal("25.00"), product.price)
    }

    @Test fun unknownFieldsFromTheBackendAreIgnored() {
        val product = decode("""{"id":1,"sku":"A","name":"A","description":"A","price":1,"stock":1,"imageKey":"x","newField":true}""")
        assertEquals(1L, product.id)
    }

    @Test fun aSerializedPriceKeepsItsPlainText() {
        val dto = decode("""{"id":1,"sku":"A","name":"A","description":"A","price":59.99,"stock":10,"imageKey":"x"}""")
        assertTrue(AppJson.encodeToString(ProductDto.serializer(), dto).contains("\"price\":59.99"))
    }

    @Test fun moneyIsShownAsUsd() {
        assertEquals("$59.99", BigDecimal("59.99").usd())
        assertEquals("$25.00", BigDecimal("25.00").usd())
        assertEquals("$1,234.50", BigDecimal("1234.5").usd())
    }

    @Test fun soldOutOnlyWithZeroStock() {
        val soldOut = decode("""{"id":3,"sku":"A","name":"A","description":"A","price":149.0,"stock":0,"imageKey":"smartwatch"}""").toModel()
        val available = decode("""{"id":5,"sku":"A","name":"A","description":"A","price":10.0,"stock":1,"imageKey":"speaker"}""").toModel()
        assertTrue(soldOut.isSoldOut)
        assertFalse(available.isSoldOut)
    }
}
