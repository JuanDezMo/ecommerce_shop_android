package co.com.learningswift.shop.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.com.learningswift.shop.core.design.ShopSnackbarHost
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.features.account.AccountScreen
import co.com.learningswift.shop.features.account.AccountViewModel
import co.com.learningswift.shop.features.cart.CartScreen
import co.com.learningswift.shop.features.catalog.CatalogScreen

/** Raíz de la app: aplica el tema que eligió el usuario y elige entre el flujo de autenticación y las pestañas según la sesión */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShopApp(container: AppContainer) {
    val themeMode by container.settings.themeMode.collectAsStateWithLifecycle()

    ShopTheme(mode = themeMode) {
        // testTagsAsResourceId se activa una sola vez, en la raíz: así Appium y Maestro ven los IDs
        Surface(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }, color = MaterialTheme.colorScheme.background) {
            val session by container.session.state.collectAsStateWithLifecycle()
            // La raíz cambia sola en cuanto la sesión cambia (inicio, logout o expiración)
            if (session.isLoggedIn) MainTabs(container) else AuthHost(container)
        }
    }
}

@Composable
private fun MainTabs(container: AppContainer) {
    val navController = rememberNavController()
    val selectedTab by container.selectedTab.collectAsStateWithLifecycle()

    // La barra solo escribe selectedTab; aquí se traduce a navegación (así cualquier pantalla puede cambiar de pestaña)
    LaunchedEffect(selectedTab) {
        navController.navigate(selectedTab.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        snackbarHost = { ShopSnackbarHost(container.snackbar) },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { container.selectedTab.value = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag(tab.tag),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = Tab.Catalog.route, modifier = Modifier.padding(padding)) {
            composable(Tab.Catalog.route) { CatalogScreen() }
            composable(Tab.Cart.route) { CartScreen() }
            composable(Tab.Account.route) {
                // Cada pantalla recibe SU ViewModel (no el contenedor): la factory saca de él lo que necesita
                val viewModel: AccountViewModel = viewModel(factory = accountViewModelFactory(container))
                AccountScreen(viewModel)
            }
        }
    }
}

