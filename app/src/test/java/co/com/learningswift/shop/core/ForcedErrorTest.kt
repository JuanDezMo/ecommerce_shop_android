package co.com.learningswift.shop.core

import co.com.learningswift.shop.core.testing.ForcedError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ForcedErrorTest {
    @Test fun aFiveHundredEmailForcesThatStatus() {
        assertEquals(503, ForcedError.status("force503@shoplab.dev"))
    }

    @Test fun caseAndSpacesAreIgnored() {
        assertEquals(418, ForcedError.status(" FORCE418@Example.com "))
    }

    @Test fun statusesOutsideFourHundredToFiveNinetyNineAreNormalEmails() {
        assertNull(ForcedError.status("force200@shoplab.dev"))
        assertNull(ForcedError.status("force700@shoplab.dev"))
    }

    @Test fun emailsWithoutTheShapeAreNormalEmails() {
        assertNull(ForcedError.status("force@shoplab.dev"))
        assertNull(ForcedError.status("forcex@shoplab.dev"))
        assertNull(ForcedError.status("standard@shoplab.dev"))
    }

    @Test fun headersCarryTheForcedStatus() {
        assertEquals(mapOf("X-Force-Error" to "503"), ForcedError.headersFor("force503@shoplab.dev"))
        assertEquals(emptyMap(), ForcedError.headersFor("standard@shoplab.dev"))
    }
}
