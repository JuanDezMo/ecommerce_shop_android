package co.com.learningswift.shop.features.register

import co.com.learningswift.shop.core.network.AppJson
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.Plan
import co.com.learningswift.shop.data.remote.dto.RegisterRequestDto
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RegisterRequestDtoTest {
    private val form = RegisterForm(
        firstName = "Alexandria", lastName = "Montgomery", email = "ana@example.com",
        password = "Secret_123", phone = "+5733001234567", country = Country.CO, termsAccepted = true,
    )

    private fun json(form: RegisterForm): JsonObject =
        AppJson.parseToJsonElement(AppJson.encodeToString(RegisterRequestDto.serializer(), RegisterRequestDto.fromForm(form))).jsonObject

    private fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.content

    @Test fun theInitialDateIsSentAsPlainText() {
        assertEquals("2000-01-01", json(form).text("birthDate"))
    }

    @Test fun theDateDoesNotDependOnTheDeviceZoneOrLanguage() {
        val zone = TimeZone.getDefault()
        val locale = Locale.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))   // UTC+14
            Locale.setDefault(Locale.forLanguageTag("ar-SA"))                 // dígitos y calendario distintos
            assertEquals("2000-01-01", json(form).text("birthDate"))
            assertEquals("1990-03-15", json(form.copy(birthDay = 15, birthMonth = 3, birthYear = 1990)).text("birthDate"))
        } finally {
            TimeZone.setDefault(zone)
            Locale.setDefault(locale)
        }
    }

    @Test fun thePlusOfThePhoneIsRemoved() {
        assertEquals("5733001234567", json(form).text("phone"))
        assertEquals("5733001234567", json(form.copy(phone = " +5733001234567 ")).text("phone"))
    }

    @Test fun anEmptyPhoneIsNotSent() {
        assertFalse(json(form.copy(phone = "  ")).containsKey("phone"))
    }

    @Test fun theEmailIsLowercaseAndTheNamesAreTrimmed() {
        val body = json(form.copy(email = "  Ana@X.COM ", firstName = " ana maria lopez ", lastName = " Montgomery "))
        assertEquals("ana@x.com", body.text("email"))
        assertEquals("ana maria lopez", body.text("firstName"))
        assertEquals("Montgomery", body.text("lastName"))
    }

    @Test fun thePasswordIsSentAsTyped() {
        assertEquals(" Secret_123 ", json(form.copy(password = " Secret_123 ")).text("password"))
    }

    @Test fun planCountryLevelAndBooleansUseTheBackendValues() {
        val body = json(form.copy(plan = Plan.PRO, experienceLevel = 5, newsletter = true))
        assertEquals("PRO", body.text("plan"))
        assertEquals("CO", body.text("country"))
        assertEquals(5, body.getValue("experienceLevel").jsonPrimitive.int)
        assertEquals(true, body.getValue("newsletter").jsonPrimitive.boolean)
        assertEquals(true, body.getValue("termsAccepted").jsonPrimitive.boolean)
    }
}
