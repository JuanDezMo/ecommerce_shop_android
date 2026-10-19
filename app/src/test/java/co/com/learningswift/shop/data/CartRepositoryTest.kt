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

    // ---- A6: editar, quitar, vaciar y ajustar al stock ----
    @Test fun reconcileLowersAQuantityToTheStock() {
        cart.add(product(5, stock = 99), 5)
        assertTrue(cart.reconcile(listOf(product(5, stock = 1))))
        assertEquals(1, cart.quantityOf(5))
    }

    @Test fun reconcileRemovesSoldOutAndMissingProducts() {
        cart.add(product(1), 2)
        cart.add(product(3, stock = 99), 1)
        cart.add(product(9), 1)
        assertTrue(cart.reconcile(listOf(product(1), product(3, stock = 0))))   // el 9 ya no existe
        assertEquals(listOf(1L), cart.items.value.map { it.productId })
    }

    @Test fun reconcileReturnsFalseAndDoesNotSaveWhenNothingChanged() {
        cart.add(product(5, stock = 1), 1)
        store.putString("cart.items", "marca")   // si guardara, la marca desaparecería
        assertFalse(cart.reconcile(listOf(product(5, stock = 1))))
        assertEquals("marca", store.getString("cart.items"))
        assertEquals(1, cart.quantityOf(5))
    }

    @Test fun setQuantityNeverExceedsTheStock() {
        val p = product(1, stock = 3)
        cart.add(p, 3)
        cart.setQuantity(9, productId = 1, stock = 3)
        assertEquals(3, cart.quantityOf(1))
    }

    @Test fun setQuantityNeverExceedsTheTwentyUnitLimit() {
        cart.add(product(1, stock = 99), 2)
        cart.add(product(2, stock = 99), 16)      // 18 en total
        cart.setQuantity(10, productId = 1, stock = 99)
        assertEquals(4, cart.quantityOf(1))       // 2 + lo que faltaba para 20
        assertEquals(20, cart.totalQuantity)
    }

    @Test fun setQuantityNeverGoesBelowOne() {
        cart.add(product(1), 2)
        cart.setQuantity(0, productId = 1, stock = 10)
        assertEquals(1, cart.quantityOf(1))
    }

    @Test fun setQuantitySavesTheChange() {
        cart.add(product(1), 2)
        cart.setQuantity(4, productId = 1, stock = 10)
        assertEquals(4, CartRepository(store).quantityOf(1))
    }

    @Test fun removeDeletesOnlyThatLine() {
        cart.add(product(1), 2)
        cart.add(product(2), 1)
        cart.remove(1)
        assertEquals(listOf(2L), cart.items.value.map { it.productId })
        assertEquals(listOf(2L), CartRepository(store).items.value.map { it.productId })
    }

    @Test fun clearEmptiesTheCartAndSavesItEmpty() {
        cart.add(product(1), 2)
        cart.clear()
        assertTrue(cart.items.value.isEmpty())
        assertTrue(CartRepository(store).items.value.isEmpty())
    }
}
