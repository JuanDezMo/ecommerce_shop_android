package co.com.learningswift.shop.core.errors

/**
 * Textos fijos de alertas y diálogos. Son contrato con las suites de Maestro y Appium
 * (catalogo-ids-accesibilidad.md): no cambian sin actualizar el catálogo.
 */
object AlertText {
    object GenericError {
        const val TITLE = "Something went wrong"
        const val RETRY = "Try again"
        const val CANCEL = "Cancel"
    }

    object ClearCart {
        const val TITLE = "Remove all items from your cart?"
        const val CONFIRM = "Clear cart"
        const val CANCEL = "Cancel"
    }

    object CartFull {
        const val TITLE = "Cart is full"
        const val MESSAGE = "Your cart can hold up to 20 items."
        const val OK = "OK"
    }

    object AccountCreated {
        const val TITLE = "Account created"
        const val MESSAGE = "You can now log in with your email."
        const val LOGIN = "Log in"
    }

    object Snackbar {
        const val ADDED_TO_CART = "Added to cart"
        const val CART_UPDATED = "Your cart was updated to match available stock."
    }
}
