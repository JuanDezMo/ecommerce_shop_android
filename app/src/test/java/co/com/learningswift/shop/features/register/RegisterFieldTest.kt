package co.com.learningswift.shop.features.register

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegisterFieldTest {
    @Test fun theOrderIsTheOrderOfTheForm() {
        assertEquals(
            listOf("FIRST_NAME", "LAST_NAME", "EMAIL", "PASSWORD", "PHONE", "BIRTH_DATE", "COUNTRY", "PLAN", "EXPERIENCE", "TERMS"),
            RegisterField.entries.map { it.name },
        )
    }

    @Test fun theFirstFiveFieldsBelongToTheFirstStep() {
        assertEquals(
            listOf(RegisterField.FIRST_NAME, RegisterField.LAST_NAME, RegisterField.EMAIL, RegisterField.PASSWORD, RegisterField.PHONE),
            RegisterField.entries.filter { it.step == RegisterStep.BASICS },
        )
        assertEquals(RegisterStep.DETAILS, RegisterField.TERMS.step)
    }

    @Test fun tagSuffixAndBackendNameOfEachField() {
        assertEquals("firstname", RegisterField.FIRST_NAME.tagSuffix)
        assertEquals("firstName", RegisterField.FIRST_NAME.backendName)
        assertEquals("birthdate", RegisterField.BIRTH_DATE.tagSuffix)
        assertEquals("birthDate", RegisterField.BIRTH_DATE.backendName)
        assertEquals("experience", RegisterField.EXPERIENCE.tagSuffix)
        assertEquals("experienceLevel", RegisterField.EXPERIENCE.backendName)
        assertEquals("terms", RegisterField.TERMS.tagSuffix)
        assertEquals("termsAccepted", RegisterField.TERMS.backendName)
    }

    @Test fun backendNamesAreTranslatedToFields() {
        assertEquals(RegisterField.EXPERIENCE, RegisterField.fromBackend("experienceLevel"))
        assertNull(RegisterField.fromBackend("nickname"))
    }
}
