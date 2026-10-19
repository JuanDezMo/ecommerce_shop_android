package co.com.learningswift.shop.core.design

import co.com.learningswift.shop.R
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProductImageTest {
    private val keys = listOf("headphones", "keyboard", "smartwatch", "mouse", "speaker", "backpack")

    @Test fun eachKnownKeyHasItsOwnImageDifferentFromThePlaceholder() {
        val resources = keys.map { ProductImage.resourceFor(it) }
        assertEquals(keys.size, resources.toSet().size)
        resources.forEach { assertNotEquals(R.drawable.product_placeholder, it) }
    }

    @Test fun theNamedResourcesAreTheExpectedOnes() {
        assertEquals(R.drawable.product_headphones, ProductImage.resourceFor("headphones"))
        assertEquals(R.drawable.product_backpack, ProductImage.resourceFor("backpack"))
    }

    @Test fun anUnknownKeyUsesThePlaceholder() {
        assertEquals(R.drawable.product_placeholder, ProductImage.resourceFor("unknown"))
    }

    @Test fun anEmptyKeyUsesThePlaceholder() {
        assertEquals(R.drawable.product_placeholder, ProductImage.resourceFor(""))
    }

    @Test fun theKeyIsCaseSensitive() {
        assertEquals(R.drawable.product_placeholder, ProductImage.resourceFor("Headphones"))
    }
}
