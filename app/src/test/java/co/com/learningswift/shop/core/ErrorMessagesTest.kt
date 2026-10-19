package co.com.learningswift.shop.core

import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.errors.ErrorMessages
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMessagesTest {
    private fun api(status: Int, code: String) = AppError.Api(status, code, "technical message", emptyMap())

    @Test fun knownCodesHaveTheirOwnMessage() {
        assertEquals("The email or password is incorrect.", api(401, "INVALID_CREDENTIALS").userMessage)
        assertEquals("Your account is locked. Please contact support.", api(423, "USER_LOCKED").userMessage)
        assertEquals("This product is no longer available.", api(404, "PRODUCT_NOT_FOUND").userMessage)
    }

    @Test fun theTechnicalMessageIsNeverShown() {
        assertEquals(ErrorMessages.GENERIC, api(400, "SOMETHING_NEW").userMessage)
    }

    @Test fun anUnknownCodeWithServerStatusUsesTheServerMessage() {
        assertEquals(ErrorMessages.SERVER, api(503, "FORCED_ERROR").userMessage)
    }

    @Test fun nonApiErrorsHaveFixedMessages() {
        assertEquals("Check your internet connection and try again.", AppError.Network.userMessage)
        assertEquals("Your session has expired. Please log in again.", AppError.SessionExpired.userMessage)
        assertEquals("Please log in to continue.", AppError.NotAuthenticated.userMessage)
        assertEquals("Something unexpected happened. Please try again.", AppError.Unknown.userMessage)
    }

    @Test fun everyBackendCodeOfTheGuideHasAMessage() {
        val codes = listOf(
            "VALIDATION_ERROR", "INVALID_CREDENTIALS", "USER_LOCKED", "EMAIL_ALREADY_EXISTS", "UNDERAGE_USER",
            "TERMS_NOT_ACCEPTED", "PRODUCT_NOT_FOUND", "INSUFFICIENT_STOCK", "EMPTY_ORDER", "DUPLICATE_ORDER_ITEM",
            "ORDER_NOT_FOUND",
        )
        codes.forEach { code -> assert(ErrorMessages.byCode.containsKey(code)) { "Falta el mensaje de $code" } }
    }
}
