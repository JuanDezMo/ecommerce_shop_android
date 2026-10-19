package co.com.learningswift.shop.features.login

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginUiStateTest {
    @Test fun disabledWithEmptyFields() {
        assertFalse(LoginUiState().isLoginEnabled)
    }

    @Test fun disabledWhenOnlyTheEmailIsBlank() {
        assertFalse(LoginUiState(email = "   ", password = "Secret_123").isLoginEnabled)
    }

    @Test fun disabledWhenOnlyThePasswordIsBlank() {
        assertFalse(LoginUiState(email = "a@b.com", password = "   ").isLoginEnabled)
    }

    @Test fun enabledWithTextInBoth() {
        assertTrue(LoginUiState(email = "a@b.com", password = "x").isLoginEnabled)
    }

    @Test fun disabledWhileLoading() {
        assertFalse(LoginUiState(email = "a@b.com", password = "x", isLoading = true).isLoginEnabled)
    }
}
