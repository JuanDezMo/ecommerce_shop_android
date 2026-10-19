package co.com.learningswift.shop.features.register

import co.com.learningswift.shop.data.model.Country
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegisterValidatorTest {
    private val today = LocalDate.of(2026, 10, 11)
    private val valid = RegisterForm(
        firstName = "Alexandria", lastName = "Montgomery", email = "ana@example.com",
        password = "Secret_123", phone = "+5733001234567", country = Country.CO, termsAccepted = true,
    )

    private fun errors(form: RegisterForm, on: LocalDate = today) = RegisterValidator.validate(form, on)
    private fun error(field: RegisterField, form: RegisterForm, on: LocalDate = today) = errors(form, on)[field]

    private val onlyLetters = "First name can only contain letters (A-Z, a-z) and single spaces between words."
    private val wrongLength = "First name must be 10 to 30 characters."

    @Test fun aCompleteValidFormHasNoErrors() {
        assertTrue(errors(valid).isEmpty())
    }

    // ---- nombres ----
    @Test fun emptyFirstNameOrOnlySpaces() {
        assertEquals("Enter your first name.", error(RegisterField.FIRST_NAME, valid.copy(firstName = "")))
        assertEquals("Enter your first name.", error(RegisterField.FIRST_NAME, valid.copy(firstName = "   ")))
        assertEquals("Enter your last name.", error(RegisterField.LAST_NAME, valid.copy(lastName = "")))
    }

    @Test fun firstNameWithForbiddenCharacters() {
        for (name in listOf("Alexandri4", "Fernándezzz", "Ana-Maria Lopez", "Ana  Maria Lopez")) {
            assertEquals(onlyLetters, error(RegisterField.FIRST_NAME, valid.copy(firstName = name)), name)
        }
        assertEquals(
            "Last name can only contain letters (A-Z, a-z) and single spaces between words.",
            error(RegisterField.LAST_NAME, valid.copy(lastName = "Alexandri4")),
        )
    }

    @Test fun firstNameLengthLimits() {
        assertEquals(wrongLength, error(RegisterField.FIRST_NAME, valid.copy(firstName = "Alexandra")))      // 9
        assertEquals(wrongLength, error(RegisterField.FIRST_NAME, valid.copy(firstName = "Ana Maria")))      // 9 con espacio
        assertEquals(wrongLength, error(RegisterField.FIRST_NAME, valid.copy(firstName = "A".repeat(31))))
        assertNull(error(RegisterField.FIRST_NAME, valid.copy(firstName = "A".repeat(10))))
        assertNull(error(RegisterField.FIRST_NAME, valid.copy(firstName = "A".repeat(30))))
        assertEquals("Last name must be 10 to 30 characters.", error(RegisterField.LAST_NAME, valid.copy(lastName = "Short")))
    }

    @Test fun onlyTheFirstErrorOfANameIsShown() {
        assertEquals(onlyLetters, error(RegisterField.FIRST_NAME, valid.copy(firstName = "Ana1")))   // corto Y con número
    }

    @Test fun namesAreTrimmedButKeepTheSpaceBetweenWords() {
        assertNull(error(RegisterField.FIRST_NAME, valid.copy(firstName = " ana maria lopez ")))
        assertNull(error(RegisterField.FIRST_NAME, valid.copy(firstName = "Ana Maria Lopez")))
        assertNull(error(RegisterField.FIRST_NAME, valid.copy(firstName = " Alexandria ")))
    }

    // ---- email ----
    @Test fun emptyEmail() {
        assertEquals("Enter your email.", error(RegisterField.EMAIL, valid.copy(email = "")))
    }

    @Test fun emailWithForbiddenCharacters() {
        for (email in listOf("ana_123@x.com", "ana-1@x.com", "ana+1@x.com", "ana 1@x.com")) {
            assertEquals("Email can only contain letters, numbers, @ and a dot.", error(RegisterField.EMAIL, valid.copy(email = email)), email)
        }
    }

    @Test fun emailWithTheWrongShape() {
        for (email in listOf("anaxxxxxxx", "ana@xxxxxx", "ana@@x.com", "@x.com", "ana@.com")) {
            assertEquals("Enter a valid email address.", error(RegisterField.EMAIL, valid.copy(email = email)), email)
        }
    }

    @Test fun emailLengthLimits() {
        assertEquals("Email must be 8 to 20 characters.", error(RegisterField.EMAIL, valid.copy(email = "a@bc.de")))                 // 7
        assertEquals("Email must be 8 to 20 characters.", error(RegisterField.EMAIL, valid.copy(email = "abcdefghij@abcdefg.co")))  // 21
        assertNull(error(RegisterField.EMAIL, valid.copy(email = "ana@x.co")))                                                      // 8
        assertNull(error(RegisterField.EMAIL, valid.copy(email = "abcdefghij@abcdef.co")))                                          // 20
        assertNull(error(RegisterField.EMAIL, valid.copy(email = "  ANA@X.COM "))) // se recorta y las mayúsculas valen
    }

    // ---- contraseña ----
    @Test fun emptyPassword() {
        assertEquals("Enter a password.", error(RegisterField.PASSWORD, valid.copy(password = "")))
    }

    @Test fun passwordLengthLimits() {
        val message = "Password must be 10 to 20 characters."
        assertEquals(message, error(RegisterField.PASSWORD, valid.copy(password = "Secret_12")))                // 9
        assertEquals(message, error(RegisterField.PASSWORD, valid.copy(password = "Secret_1234567890123A")))    // 21
        assertNull(error(RegisterField.PASSWORD, valid.copy(password = "Secret_123")))                          // 10
        assertNull(error(RegisterField.PASSWORD, valid.copy(password = "Secret_1234567890123")))                // 20
    }

    @Test fun passwordComposition() {
        val message = "Use an uppercase letter, a lowercase letter, a number and a special character, without spaces."
        for (password in listOf("secret_123", "SECRET_123", "Secret_abc", "Secret1234", "Secret 123", "Secret_ñ12")) {
            assertEquals(message, error(RegisterField.PASSWORD, valid.copy(password = password)), password)
        }
    }

    @Test fun thePasswordIsNotTrimmed() {
        assertEquals(
            "Use an uppercase letter, a lowercase letter, a number and a special character, without spaces.",
            error(RegisterField.PASSWORD, valid.copy(password = " Secret_123")),
        )
    }

    // ---- teléfono ----
    @Test fun emptyPhone() {
        assertEquals("Enter your phone number.", error(RegisterField.PHONE, valid.copy(phone = "")))
        assertEquals("Enter your phone number.", error(RegisterField.PHONE, valid.copy(phone = "  ")))
    }

    @Test fun phoneWithTheWrongFormat() {
        val message = "Use the format +CCC followed by 10 digits, for example +5733001234567."
        for (phone in listOf("3001234567", "+573300123456", "+57330012345678", "+57 3300 1234567", "+57-3300123456")) {
            assertEquals(message, error(RegisterField.PHONE, valid.copy(phone = phone)), phone)
        }
    }

    @Test fun validPhones() {
        assertNull(error(RegisterField.PHONE, valid.copy(phone = "+5733001234567")))
        assertNull(error(RegisterField.PHONE, valid.copy(phone = " +5733001234567 ")))
    }

    // ---- edad ----
    private val underage = "You must be at least 18 years old to register."

    @Test fun turning18TodayIsAllowedAndTomorrowIsNot() {
        assertNull(error(RegisterField.BIRTH_DATE, valid.copy(birthYear = 2008, birthMonth = 10, birthDay = 11)))
        assertEquals(underage, error(RegisterField.BIRTH_DATE, valid.copy(birthYear = 2008, birthMonth = 10, birthDay = 12)))
    }

    @Test fun bornOnFebruary29IsAnAdultOnlyFromMarch1() {
        val born = valid.copy(birthYear = 2008, birthMonth = 2, birthDay = 29)
        assertEquals(underage, error(RegisterField.BIRTH_DATE, born, on = LocalDate.of(2026, 2, 28)))
        assertNull(error(RegisterField.BIRTH_DATE, born, on = LocalDate.of(2026, 3, 1)))
    }

    @Test fun theInitialDateIsValid() {
        assertNull(error(RegisterField.BIRTH_DATE, valid))
    }

    // ---- país y términos ----
    @Test fun countryAndTermsAreRequired() {
        assertEquals("Select your country.", error(RegisterField.COUNTRY, valid.copy(country = null)))
        assertEquals("You must accept the terms and conditions.", error(RegisterField.TERMS, valid.copy(termsAccepted = false)))
    }

    @Test fun anEmptyFormHasAnErrorInEveryRequiredField() {
        val fields = errors(RegisterForm()).keys
        assertEquals(
            setOf(
                RegisterField.FIRST_NAME, RegisterField.LAST_NAME, RegisterField.EMAIL, RegisterField.PASSWORD,
                RegisterField.PHONE, RegisterField.COUNTRY, RegisterField.TERMS,
            ),
            fields,
        )
    }
}
