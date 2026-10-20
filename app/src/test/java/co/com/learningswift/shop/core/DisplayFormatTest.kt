package co.com.learningswift.shop.core

import co.com.learningswift.shop.core.formatting.DisplayFormat
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DisplayFormatTest {
    private val instant = Instant.parse("2026-09-01T03:00:00Z")
    private val bogota = ZoneId.of("America/Bogota")        // UTC-5: es el día anterior
    private val auckland = ZoneId.of("Pacific/Auckland")    // UTC+12: es el mismo día

    @Test fun listDateInTheDeviceZone() {
        assertEquals("Aug 31, 2026", DisplayFormat.listDate(instant, bogota))
        assertEquals("Sep 1, 2026", DisplayFormat.listDate(instant, auckland))
    }

    @Test fun detailDateHasTheTimeInTwelveHours() {
        assertEquals("Aug 31, 2026, 10:00 PM", DisplayFormat.detailDate(instant, bogota))
        assertEquals("Sep 1, 2026, 3:00 PM", DisplayFormat.detailDate(instant, auckland))
    }

    @Test fun beforeAmOrPmThereIsANormalSpace() {
        val text = DisplayFormat.detailDate(Instant.parse("2026-09-15T11:30:00Z"), ZoneId.of("UTC"))
        assertEquals("Sep 15, 2026, 11:30 AM", text)
        assertFalse(text.contains(' '))
        assertFalse(text.contains(' '))
    }

    @Test fun unitsUseTheSingularOnlyForOne() {
        assertEquals("1 item", DisplayFormat.units(1))
        assertEquals("3 items", DisplayFormat.units(3))
    }

    @Test fun theStatusIsReadable() {
        assertEquals("Confirmed", DisplayFormat.status("CONFIRMED"))
    }
}
