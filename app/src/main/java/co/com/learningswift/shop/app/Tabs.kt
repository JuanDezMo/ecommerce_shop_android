package co.com.learningswift.shop.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import co.com.learningswift.shop.core.testing.TestTags

/** Las tres pestañas principales. `route` es el destino de navegación y `tag` el ID del catálogo */
enum class Tab(val route: String, val label: String, val icon: ImageVector, val tag: String) {
    Catalog("catalog", "Catalog", Icons.Filled.Home, TestTags.App.TAB_CATALOG),
    Cart("cart", "Cart", Icons.Filled.ShoppingCart, TestTags.App.TAB_CART),
    Account("account", "Account", Icons.Filled.Person, TestTags.App.TAB_ACCOUNT),
}
