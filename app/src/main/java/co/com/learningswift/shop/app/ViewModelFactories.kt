package co.com.learningswift.shop.app

import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import co.com.learningswift.shop.features.account.AccountViewModel
import co.com.learningswift.shop.features.cart.CartViewModel
import co.com.learningswift.shop.features.catalog.CatalogViewModel
import co.com.learningswift.shop.features.checkout.CheckoutViewModel
import co.com.learningswift.shop.features.login.LoginViewModel
import co.com.learningswift.shop.features.productdetail.ProductDetailViewModel
import co.com.learningswift.shop.features.register.RegisterViewModel

/** Cada pantalla recibe SU ViewModel (no el contenedor): la fábrica saca de él lo que necesita. Todas aquí, en un solo archivo */
fun accountViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { AccountViewModel(container.settings) }
}

fun loginViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { LoginViewModel(container.authRepository) }
}

fun registerViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { RegisterViewModel(container.userRepository) }
}

fun catalogViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { CatalogViewModel(container.productRepository) }
}

fun productDetailViewModelFactory(container: AppContainer, productId: Long) = viewModelFactory {
    initializer { ProductDetailViewModel(container.productRepository, productId) }
}

fun cartViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { CartViewModel(container.cart, container.productRepository, container.snackbar) }
}

fun checkoutViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { CheckoutViewModel(container.cart, container.orderRepository) }
}
