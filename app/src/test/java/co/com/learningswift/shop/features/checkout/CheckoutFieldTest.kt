package co.com.learningswift.shop.features.checkout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CheckoutFieldTest {
    @Test fun theOrderIsTheOrderOfTheForm() {
        assertEquals(listOf("ADDRESS", "CITY", "POSTAL_CODE", "COUNTRY", "PAYMENT"), CheckoutField.entries.map { it.name })
    }

    @Test fun tagSuffixAndBackendNameOfEachField() {
        assertEquals("address", CheckoutField.ADDRESS.tagSuffix)
        assertEquals("shipping.address", CheckoutField.ADDRESS.backendName)
        assertEquals("postalcode", CheckoutField.POSTAL_CODE.tagSuffix)
        assertEquals("shipping.postalCode", CheckoutField.POSTAL_CODE.backendName)
        assertEquals("country", CheckoutField.COUNTRY.tagSuffix)
        assertEquals("shipping.country", CheckoutField.COUNTRY.backendName)
        assertEquals("payment", CheckoutField.PAYMENT.tagSuffix)
        assertEquals("paymentMethod", CheckoutField.PAYMENT.backendName)
    }

    @Test fun backendNamesAreTranslatedToFields() {
        assertEquals(CheckoutField.POSTAL_CODE, CheckoutField.fromBackend("shipping.postalCode"))
        assertNull(CheckoutField.fromBackend("items[0].quantity"))
        assertNull(CheckoutField.fromBackend("shipping"))
    }
}
