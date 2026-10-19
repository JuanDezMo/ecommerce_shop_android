package co.com.learningswift.shop.data

import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.Plan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CountryAndPlanTest {
    @Test fun sixCountriesWithTheirBackendCodes() {
        assertEquals(listOf("AR", "CL", "CO", "MX", "PE", "US"), Country.entries.map { it.code }.sorted())
    }

    @Test fun namesAreInEnglishAndSortedAlphabetically() {
        assertEquals(
            listOf("Argentina", "Chile", "Colombia", "Mexico", "Peru", "United States"),
            Country.sorted.map { it.displayName },
        )
    }

    @Test fun spainIsNotOffered() {
        assertFalse(Country.entries.any { it.code == "ES" })
    }

    @Test fun plansUseTheBackendValueAndAReadableLabel() {
        assertEquals("BASIC", Plan.BASIC.name)
        assertEquals("Basic", Plan.BASIC.label)
        assertEquals("Pro", Plan.PRO.label)
    }
}
