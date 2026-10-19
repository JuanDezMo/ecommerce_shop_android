package co.com.learningswift.shop.features.productdetail

import co.com.learningswift.shop.data.local.InMemoryStore
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.CartRepository
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class AddToCartActionTest {
    private val cart = CartRepository(InMemoryStore())

    private fun product(id: Long, stock: Int) =
        Product(id, "SKU-$id", "Product $id", "Description", BigDecimal("10.00"), stock, "mouse")

    @Test fun aSoldOutProductIsDisabled() {
        assertEquals(AddToCartAction.Disabled, addToCartAction(product(1, stock = 0), cart))
    }

    @Test fun aProductWithAllItsStockInTheCartIsDisabled() {
        val p = product(1, stock = 2)
        cart.add(p, 2)
        assertEquals(AddToCartAction.Disabled, addToCartAction(p, cart))
    }

    @Test fun withStockAndAFullCartItShowsTheCartFullAlert() {
        cart.add(product(1, stock = 99), 20)
        assertEquals(AddToCartAction.CartFull, addToCartAction(product(2, stock = 5), cart))
    }

    @Test fun aProductWithStockLeftButAFullCartIsCartFull() {
        val p = product(1, stock = 5)
        cart.add(p, 4)
        cart.add(product(2, stock = 99), 16)      // 20 en total
        assertEquals(AddToCartAction.CartFull, addToCartAction(p, cart))
    }

    @Test fun otherwiseItOpensTheSheet() {
        assertEquals(AddToCartAction.OpenSheet, addToCartAction(product(1, stock = 10), cart))
    }
}
