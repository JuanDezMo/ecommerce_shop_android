package co.com.learningswift.shop.features.checkout

import co.com.learningswift.shop.data.model.Country
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckoutValidatorTest {
    private val valid = CheckoutForm(address = "Calle 10 #20-30", city = "Bogotá", postalCode = "110111", country = Country.CO)

    private fun error(field: CheckoutField, form: CheckoutForm) = CheckoutValidator.validate(form)[field]

    private val addressLength = "Address must be 5 to 120 characters."
    private val postalFormat = "Use 3 to 10 letters, numbers, spaces or hyphens."

    @Test fun aValidFormHasNoErrors() {
        assertTrue(CheckoutValidator.validate(valid).isEmpty())
    }

    @Test fun addressLengthLimits() {
        assertEquals(addressLength, error(CheckoutField.ADDRESS, valid.copy(address = "Abcd")))              // 4
        assertNull(error(CheckoutField.ADDRESS, valid.copy(address = "Abcde")))                              // 5
        assertNull(error(CheckoutField.ADDRESS, valid.copy(address = "A".repeat(120))))
        assertEquals(addressLength, error(CheckoutField.ADDRESS, valid.copy(address = "A".repeat(121))))
    }

    @Test fun anEmptyAddressOrOnlySpaces() {
        assertEquals("Enter your address.", error(CheckoutField.ADDRESS, valid.copy(address = "")))
        assertEquals("Enter your address.", error(CheckoutField.ADDRESS, valid.copy(address = "     ")))
    }

    @Test fun cityLengthLimits() {
        val message = "City must be 2 to 60 characters."
        assertEquals("Enter your city.", error(CheckoutField.CITY, valid.copy(city = "")))
        assertEquals(message, error(CheckoutField.CITY, valid.copy(city = "A")))
        assertNull(error(CheckoutField.CITY, valid.copy(city = "Al")))
        assertNull(error(CheckoutField.CITY, valid.copy(city = "A".repeat(60))))
        assertEquals(message, error(CheckoutField.CITY, valid.copy(city = "A".repeat(61))))
    }

    @Test fun postalCodeFormat() {
        assertEquals("Enter your postal code.", error(CheckoutField.POSTAL_CODE, valid.copy(postalCode = "")))
        assertNull(error(CheckoutField.POSTAL_CODE, valid.copy(postalCode = "SW1A 1AA")))
        assertNull(error(CheckoutField.POSTAL_CODE, valid.copy(postalCode = "12-345")))
        for (code in listOf("12", "12#45", "12345678901")) {
            assertEquals(postalFormat, error(CheckoutField.POSTAL_CODE, valid.copy(postalCode = code)), code)
        }
    }

    @Test fun theCountryIsRequired() {
        assertEquals("Select your country.", error(CheckoutField.COUNTRY, valid.copy(country = null)))
    }

    @Test fun textsAreTrimmedBeforeValidating() {
        assertNull(error(CheckoutField.ADDRESS, valid.copy(address = "  Calle 10  ")))
        assertEquals(addressLength, error(CheckoutField.ADDRESS, valid.copy(address = "  Abc  ")))   // recortado: 3
    }

    @Test fun anEmptyFormHasFourErrors() {
        assertEquals(
            setOf(CheckoutField.ADDRESS, CheckoutField.CITY, CheckoutField.POSTAL_CODE, CheckoutField.COUNTRY),
            CheckoutValidator.validate(CheckoutForm()).keys,
        )
    }
}
