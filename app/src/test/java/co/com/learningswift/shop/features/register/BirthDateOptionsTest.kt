package co.com.learningswift.shop.features.register

import kotlin.test.Test
import kotlin.test.assertEquals

class BirthDateOptionsTest {
    @Test fun twelveMonthsInEnglish() {
        assertEquals(12, BirthDateOptions.months.size)
        assertEquals("January", BirthDateOptions.months.first())
        assertEquals("December", BirthDateOptions.months.last())
    }

    @Test fun yearsGoFromTheCurrentOneTo1900WithoutRepeats() {
        val years = BirthDateOptions.years(currentYear = 2026)
        assertEquals(2026, years.first())
        assertEquals(1900, years.last())
        assertEquals(years.size, years.toSet().size)
    }

    @Test fun daysInMonth() {
        assertEquals(31, BirthDateOptions.daysInMonth(2000, 1))
        assertEquals(30, BirthDateOptions.daysInMonth(2000, 4))
        assertEquals(28, BirthDateOptions.daysInMonth(2023, 2))
        assertEquals(29, BirthDateOptions.daysInMonth(2024, 2))
        assertEquals(29, BirthDateOptions.daysInMonth(2000, 2))
        assertEquals(28, BirthDateOptions.daysInMonth(1900, 2))   // 1900 no fue bisiesto
    }

    @Test fun theDayDropsToTheLastOneOfTheMonth() {
        assertEquals(29, BirthDateOptions.clampedDay(31, 2000, 2))
        assertEquals(28, BirthDateOptions.clampedDay(31, 2001, 2))
        assertEquals(15, BirthDateOptions.clampedDay(15, 2001, 2))
    }
}
