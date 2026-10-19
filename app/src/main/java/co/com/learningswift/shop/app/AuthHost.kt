package co.com.learningswift.shop.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.com.learningswift.shop.features.login.LoginScreen
import co.com.learningswift.shop.features.login.LoginViewModel

/** Flujo de autenticación. Hoy solo tiene el login; el registro llega en la iteración A4 */
@Composable
fun AuthHost(container: AppContainer) {
    val session by container.session.state.collectAsStateWithLifecycle()
    val viewModel: LoginViewModel = viewModel(factory = loginViewModelFactory(container))

    LoginScreen(
        viewModel = viewModel,
        sessionExpired = session.didExpire,
        onRegisterTap = {},   // TODO(A4): navegar al registro
    )
}
