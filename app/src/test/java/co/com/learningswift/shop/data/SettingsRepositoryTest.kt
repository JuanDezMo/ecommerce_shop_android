package co.com.learningswift.shop.data

import co.com.learningswift.shop.data.local.InMemoryStore
import co.com.learningswift.shop.data.repository.SettingsRepository
import co.com.learningswift.shop.data.repository.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRepositoryTest {
    private val store = InMemoryStore()

    @Test fun defaultThemeIsSystem() {
        assertEquals(ThemeMode.SYSTEM, SettingsRepository(store).themeMode.value)
    }

    @Test fun theChosenThemeIsRecoveredByANewRepository() {
        SettingsRepository(store).setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, SettingsRepository(store).themeMode.value)
    }

    @Test fun changingTheThemeNotifiesTheFlow() {
        val settings = SettingsRepository(store)
        settings.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, settings.themeMode.value)
    }

    @Test fun anUnknownSavedValueFallsBackToSystem() {
        store.putString("settings.themeMode", "SEPIA")
        assertEquals(ThemeMode.SYSTEM, SettingsRepository(store).themeMode.value)
    }

    @Test fun themeModesAreInOrderWithTheirLabels() {
        assertEquals(listOf("System", "Light", "Dark"), ThemeMode.entries.map { it.label })
    }
}
