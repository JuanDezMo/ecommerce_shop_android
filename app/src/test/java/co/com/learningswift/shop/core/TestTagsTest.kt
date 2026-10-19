package co.com.learningswift.shop.core

import co.com.learningswift.shop.core.testing.TestTags
import java.io.File
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertTrue

/** Todo ID de la app debe existir en el catálogo compartido con iOS (docs/catalogo-ids-accesibilidad.md) */
class TestTagsTest {
    private val catalog = File("../docs/catalogo-ids-accesibilidad.md").readText()

    private fun appTags(): List<String> = TestTags::class.java.declaredClasses.flatMap { group ->
        group.declaredFields
            .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .map { it.apply { isAccessible = true }.get(null) as String }
    }

    @Test fun theAppDeclaresTags() {
        assertTrue(appTags().isNotEmpty())
    }

    @Test fun everyAppTagExistsInTheCatalog() {
        val missing = appTags().filterNot { catalog.contains("`$it`") }
        assertTrue(missing.isEmpty(), "IDs de la app que no están en el catálogo: $missing")
    }

    @Test fun tagsAreSnakeCase() {
        val invalid = appTags().filterNot { it.matches(Regex("[a-z0-9_]+")) }
        assertTrue(invalid.isEmpty(), "IDs con formato inválido: $invalid")
    }
}
