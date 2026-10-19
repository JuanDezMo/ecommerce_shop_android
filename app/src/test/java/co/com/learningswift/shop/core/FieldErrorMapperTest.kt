package co.com.learningswift.shop.core

import co.com.learningswift.shop.core.errors.FieldErrorMapper
import co.com.learningswift.shop.features.register.RegisterField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FieldErrorMapperTest {
    private fun map(backend: Map<String, List<String>>) = FieldErrorMapper.map(backend, RegisterField::fromBackend)

    @Test fun eachBackendFieldGoesToItsFormField() {
        val mapped = map(mapOf("firstName" to listOf("bad"), "termsAccepted" to listOf("must accept")))
        assertEquals(listOf("bad"), mapped.fields[RegisterField.FIRST_NAME])
        assertEquals(listOf("must accept"), mapped.fields[RegisterField.TERMS])
        assertFalse(mapped.hasUnmappedErrors)
    }

    @Test fun aFieldWithTwoMessagesKeepsBoth() {
        assertEquals(listOf("a", "b"), map(mapOf("password" to listOf("a", "b"))).fields[RegisterField.PASSWORD])
    }

    @Test fun anUnknownFieldIsFlaggedButTheKnownOnesAreMapped() {
        val mapped = map(mapOf("nickname" to listOf("x"), "email" to listOf("bad")))
        assertTrue(mapped.hasUnmappedErrors)
        assertEquals(listOf("bad"), mapped.fields[RegisterField.EMAIL])
    }

    @Test fun anEmptyMapIsFlagged() {
        assertTrue(map(emptyMap()).hasUnmappedErrors)
    }

    @Test fun checkoutFieldsAreMappedWithTheSameMapper() {
        val mapped = FieldErrorMapper.map(
            mapOf("shipping.city" to listOf("size must be between 2 and 60")),
            co.com.learningswift.shop.features.checkout.CheckoutField::fromBackend,
        )
        assertEquals(listOf("size must be between 2 and 60"), mapped.fields[co.com.learningswift.shop.features.checkout.CheckoutField.CITY])
        assertFalse(mapped.hasUnmappedErrors)
    }

    @Test fun itemErrorsOfTheCheckoutAreNotFormFields() {
        val mapped = FieldErrorMapper.map(
            mapOf("items[0].quantity" to listOf("must be at least 1")),
            co.com.learningswift.shop.features.checkout.CheckoutField::fromBackend,
        )
        assertTrue(mapped.hasUnmappedErrors)
        assertTrue(mapped.fields.isEmpty())
    }
}
