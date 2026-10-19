package co.com.learningswift.shop.app

import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import co.com.learningswift.shop.features.account.AccountViewModel
import co.com.learningswift.shop.features.login.LoginViewModel

/** Cada pantalla recibe SU ViewModel (no el contenedor): la fábrica saca de él lo que necesita. Todas aquí, en un solo archivo */
fun accountViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { AccountViewModel(container.settings) }
}

fun loginViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { LoginViewModel(container.authRepository) }
}
