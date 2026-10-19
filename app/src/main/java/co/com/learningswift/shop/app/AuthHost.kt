package co.com.learningswift.shop.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import co.com.learningswift.shop.features.login.LoginScreen
import co.com.learningswift.shop.features.login.LoginViewModel
import co.com.learningswift.shop.features.register.RegisterBasicScreen
import co.com.learningswift.shop.features.register.RegisterDetailsScreen
import co.com.learningswift.shop.features.register.RegisterViewModel

private const val LOGIN = "login"
private const val REGISTER_GRAPH = "register_graph"
private const val REGISTER_BASIC = "register/basic"
private const val REGISTER_DETAILS = "register/details"

/**
 * Flujo de autenticación: login → registro (paso 1 → paso 2).
 *
 * - `LoginViewModel` se crea aquí, FUERA del NavHost: conserva el email y la contraseña al ir al registro y volver.
 * - `RegisterViewModel` pertenece al grafo del registro: lo comparten sus dos pantallas y se destruye al salir de él,
 *   así cada visita al registro empieza con los valores iniciales.
 */
@Composable
fun AuthHost(container: AppContainer) {
    val session by container.session.state.collectAsStateWithLifecycle()
    val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory(container))
    val navController = rememberNavController()

    NavHost(navController, startDestination = LOGIN) {
        composable(LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                sessionExpired = session.didExpire,
                onRegisterTap = { navController.navigate(REGISTER_GRAPH) },
            )
        }
        navigation(startDestination = REGISTER_BASIC, route = REGISTER_GRAPH) {
            composable(REGISTER_BASIC) { entry ->
                RegisterBasicScreen(
                    viewModel = registerViewModel(navController, entry, container),
                    onNext = { navController.navigate(REGISTER_DETAILS) },
                    onLoginTap = { navController.popBackStack(LOGIN, inclusive = false) },
                )
            }
            composable(REGISTER_DETAILS) { entry ->
                RegisterDetailsScreen(
                    viewModel = registerViewModel(navController, entry, container),
                    onNeedsBasics = { navController.popBackStack() },
                    onRegistered = { email ->
                        // De vuelta en el login: el email registrado escrito y la contraseña vacía. No se inicia sesión sola
                        loginViewModel.onEmailChange(email)
                        loginViewModel.onPasswordChange("")
                        navController.popBackStack(LOGIN, inclusive = false)
                    },
                )
            }
        }
    }
}

/** El ViewModel del registro, con el alcance del grafo (no de cada pantalla) */
@Composable
private fun registerViewModel(navController: NavController, entry: NavBackStackEntry, container: AppContainer): RegisterViewModel {
    val graphEntry = remember(entry) { navController.getBackStackEntry(REGISTER_GRAPH) }
    return viewModel(graphEntry, factory = registerViewModelFactory(container))
}
