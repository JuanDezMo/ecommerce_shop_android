package co.com.learningswift.shop.core.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionManagerTest {
    private val session = SessionManager()

    @Test fun startsWithoutSession() {
        assertFalse(session.state.value.isLoggedIn)
        assertFalse(session.state.value.didExpire)
    }

    @Test fun startLogsInWithoutExpirationFlag() {
        session.start("token")
        assertTrue(session.state.value.isLoggedIn)
        assertEquals("token", session.token)
        assertFalse(session.state.value.didExpire)
    }

    @Test fun endClearsTheTokenWithoutNotice() {
        session.start("token")
        session.end()
        assertNull(session.token)
        assertFalse(session.state.value.isLoggedIn)
        assertFalse(session.state.value.didExpire)
    }

    @Test fun expireClearsTheTokenAndRaisesTheFlag() {
        session.start("token")
        session.expire()
        assertFalse(session.state.value.isLoggedIn)
        assertTrue(session.state.value.didExpire)
    }

    @Test fun startingAgainAfterExpiringClearsTheFlag() {
        session.expire()
        session.start("token")
        assertTrue(session.state.value.isLoggedIn)
        assertFalse(session.state.value.didExpire)
    }
}
