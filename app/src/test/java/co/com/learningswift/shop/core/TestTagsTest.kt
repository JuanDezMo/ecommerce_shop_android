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

    /** IDs de la primera columna de las tablas del catálogo y los de los botones de las alertas (`texto → ID`) */
    private fun catalogIds(): List<String> {
        val firstColumn = catalog.lines().mapNotNull { Regex("^\\| `([A-Za-z0-9_<>]+)` \\|").find(it)?.groupValues?.get(1) }
        val alertButtons = Regex("→ `([a-z0-9_]+)`").findAll(catalog).map { it.groupValues[1] }
        return (firstColumn + alertButtons).distinct()
    }

    /** IDs del catálogo que Android no tiene, con el motivo. Debe quedar vacío o casi */
    private val catalogOnlyInIos = emptyMap<String, String>()

    @Test fun theCatalogIdsAreRead() {
        assertTrue(catalogIds().size > 80, "Se leyeron ${catalogIds().size} IDs del catálogo")
    }

    @Test fun everyStaticCatalogIdExistsInTheApp() {
        val missing = catalogIds().filterNot { "<" in it }.filterNot { it in appTags() || it in catalogOnlyInIos }
        assertTrue(missing.isEmpty(), "IDs del catálogo que la app no tiene: $missing")
    }

    @Test fun everyCatalogTemplateHasAFunction() {
        val covered = dynamicTemplates().values.map { it.trim('`') }
        val missing = catalogIds().filter { "<" in it }.filterNot { id -> covered.any { it == id } }
        assertTrue(missing.isEmpty(), "Plantillas del catálogo sin función en TestTags: $missing")
    }

    @Test fun tagsAreSnakeCase() {
        val invalid = appTags().filterNot { it.matches(Regex("[a-z0-9_]+")) }
        assertTrue(invalid.isEmpty(), "IDs con formato inválido: $invalid")
    }

    private fun dynamicTemplates() = mapOf(
            TestTags.Catalog.item(7) to "catalog_item_<productId>`",
            TestTags.Catalog.name(7) to "catalog_item_<productId>_name_text",
            TestTags.Catalog.price(7) to "catalog_item_<productId>_price_text",
            TestTags.Catalog.soldOut(7) to "catalog_item_<productId>_soldout_text",
            TestTags.Catalog.addButton(7) to "catalog_item_<productId>_add_button",
            TestTags.productImage("mouse") to "product_image_<imageKey>",
            TestTags.Confirmation.item(7) to "confirmation_item_<productId>",
            TestTags.Orders.item(7) to "orders_item_<orderId>`",
            TestTags.Orders.date(7) to "orders_item_<orderId>_date_text",
            TestTags.Orders.total(7) to "orders_item_<orderId>_total_text",
            TestTags.Orders.count(7) to "orders_item_<orderId>_count_text",
            TestTags.OrderDetail.item(7) to "order_detail_item_<productId>`",
            TestTags.OrderDetail.subtotal(7) to "order_detail_item_<productId>_subtotal_text",
            TestTags.Cart.item(7) to "cart_item_<productId>`",
            TestTags.Cart.name(7) to "cart_item_<productId>_name_text",
            TestTags.Cart.quantityStepper(7) to "cart_item_<productId>_quantity_stepper",
            TestTags.Cart.subtotal(7) to "cart_item_<productId>_subtotal_text",
            TestTags.Cart.deleteButton(7) to "cart_item_<productId>_delete_button",
        )

    @Test fun dynamicTagsFollowTheCatalogTemplates() {
        val templates = dynamicTemplates()
        for ((tag, template) in templates) {
            assertTrue(catalog.contains(template), "El catálogo no tiene la plantilla $template")
            assertTrue(tag.matches(Regex("[a-z0-9_]+")), tag)
        }
        assertEquals("catalog_item_7_add_button", TestTags.Catalog.addButton(7))
        assertEquals("product_image_unknown", TestTags.productImage("unknown"))
    }
}
