package co.com.learningswift.shop.data

import co.com.learningswift.shop.data.local.InMemoryStore
import co.com.learningswift.shop.data.model.Product
import co.com.learningswift.shop.data.repository.CartRepository
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CartRepositoryTest {
    private val store = InMemoryStore()
    private val cart = CartRepository(store)

    private fun product(id: Long, stock: Int = 10, price: String = "59.99") =
        Product(id, "SKU-$id", "Product $id", "Description", BigDecimal(price), stock, "headphones")

    @Test fun anEmptyCartHasNothing() {
        assertTrue(cart.items.value.isEmpty())
        assertEquals(0, cart.totalQuantity)
        assertFalse(cart.isFull)
    }

    @Test fun addingAProductCreatesItsLine() {
        cart.add(product(1), 2)
        assertEquals(1, cart.items.value.size)
        assertEquals(2, cart.quantityOf(1))
    }

    @Test fun addingTheSameProductAgainAddsToItsSingleLine() {
        cart.add(product(1), 3)
        cart.add(product(1), 2)
        assertEquals(1, cart.items.value.size)
        assertEquals(5, cart.quantityOf(1))
    }

    @Test fun theStockIsALimit() {
        val single = product(1, stock = 1)
        cart.add(single, 1)
        assertEquals(0, cart.maxAddable(single))
        cart.add(single, 1)
        assertEquals(1, cart.quantityOf(1))
    }

    @Test fun theCartHoldsAtMostTwentyUnits() {
        cart.add(product(1, stock = 99), 10)
        cart.add(product(2, stock = 99), 8)       // 18 en total
        assertEquals(2, cart.maxAddable(product(3, stock = 99)))
        cart.add(product(3, stock = 99), 5)       // solo caben 2
        assertEquals(2, cart.quantityOf(3))
        assertEquals(20, cart.totalQuantity)
        assertTrue(cart.isFull)
        assertEquals(0, cart.maxAddable(product(4, stock = 99)))
    }

    @Test fun maxAddableIsTheSmallerOfStockLeftAndRoomInTheCart() {
        val p = product(1, stock = 10)
        cart.add(p, 8)
        cart.add(product(2, stock = 99), 4)       // 12 en total
        assertEquals(2, cart.maxAddable(p))       // stock: 10 - 8 = 2; cabe en el carrito: 8
    }

    @Test fun totalQuantityAddsUpAllTheLines() {
        cart.add(product(1), 2)
        cart.add(product(2), 3)
        assertEquals(5, cart.totalQuantity)
    }

    @Test fun theCartSurvivesANewRepositoryOverTheSameStoreWithTheExactPrice() {
        cart.add(product(1, price = "59.99"), 2)
        val reopened = CartRepository(store)
        assertEquals(2, reopened.quantityOf(1))
        assertEquals(BigDecimal("59.99"), reopened.items.value.single().unitPrice)
        assertEquals("Product 1", reopened.items.value.single().name)
    }

    @Test fun anUnreadableSavedCartIsAnEmptyCart() {
        store.putString("cart.items", "esto no es JSON")
        assertTrue(CartRepository(store).items.value.isEmpty())
    }
}
