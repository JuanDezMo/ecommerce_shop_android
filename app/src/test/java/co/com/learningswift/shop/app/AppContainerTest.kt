package co.com.learningswift.shop.app

import co.com.learningswift.shop.data.local.InMemoryStore
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals

class AppContainerTest {
    private fun makeContainer() = AppContainer(
        baseUrl = "http://localhost:8080/api/v1/",
        store = InMemoryStore(),
        secureStoreProvider = { InMemoryStore() },
        scope = TestScope(),
    )

    @Test fun theInitialTabIsCatalog() {
        assertEquals(Tab.Catalog, makeContainer().selectedTab.value)
    }

    @Test fun changingTheSelectedTabIsReflected() {
        val container = makeContainer()
        container.selectedTab.value = Tab.Cart
        assertEquals(Tab.Cart, container.selectedTab.value)
    }
}
