package co.com.learningswift.shop.core

import co.com.learningswift.shop.core.testing.TestTags
import java.io.File
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test fun dynamicTagsFollowTheCatalogTemplates() {
        val templates = mapOf(
            TestTags.Catalog.item(7) to "catalog_item_<productId>`",
            TestTags.Catalog.name(7) to "catalog_item_<productId>_name_text",
            TestTags.Catalog.price(7) to "catalog_item_<productId>_price_text",
            TestTags.Catalog.soldOut(7) to "catalog_item_<productId>_soldout_text",
            TestTags.Catalog.addButton(7) to "catalog_item_<productId>_add_button",
            TestTags.productImage("mouse") to "product_image_<imageKey>",
            TestTags.Confirmation.item(7) to "confirmation_item_<productId>",
            TestTags.Cart.item(7) to "cart_item_<productId>`",
            TestTags.Cart.name(7) to "cart_item_<productId>_name_text",
            TestTags.Cart.quantityStepper(7) to "cart_item_<productId>_quantity_stepper",
            TestTags.Cart.subtotal(7) to "cart_item_<productId>_subtotal_text",
            TestTags.Cart.deleteButton(7) to "cart_item_<productId>_delete_button",
        )
        for ((tag, template) in templates) {
            assertTrue(catalog.contains(template), "El catálogo no tiene la plantilla $template")
            assertTrue(tag.matches(Regex("[a-z0-9_]+")), tag)
        }
        assertEquals("catalog_item_7_add_button", TestTags.Catalog.addButton(7))
        assertEquals("product_image_unknown", TestTags.productImage("unknown"))
    }
}
