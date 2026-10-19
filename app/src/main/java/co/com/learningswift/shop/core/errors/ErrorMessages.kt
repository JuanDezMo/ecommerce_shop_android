package co.com.learningswift.shop.core.errors

/** Texto que ve el usuario. Sale de esta tabla, nunca del mensaje técnico del backend */
object ErrorMessages {
    const val GENERIC = "Something went wrong. Please try again."
    const val SERVER = "Something went wrong on our side. Please try again later."

    fun messageFor(error: AppError): String = when (error) {
        AppError.Network -> "Check your internet connection and try again."
        AppError.SessionExpired -> "Your session has expired. Please log in again."
        AppError.NotAuthenticated -> "Please log in to continue."
        AppError.Unknown -> "Something unexpected happened. Please try again."
        is AppError.Api -> byCode[error.code] ?: if (error.status >= 500) SERVER else GENERIC
    }

    val byCode: Map<String, String> = mapOf(
        "VALIDATION_ERROR" to "Some fields need your attention.",
        "INVALID_CREDENTIALS" to "The email or password is incorrect.",
        "USER_LOCKED" to "Your account is locked. Please contact support.",
        "EMAIL_ALREADY_EXISTS" to "An account with this email already exists.",
        "UNDERAGE_USER" to "You must be at least 18 years old to register.",
        "TERMS_NOT_ACCEPTED" to "You must accept the terms and conditions.",
        "PRODUCT_NOT_FOUND" to "This product is no longer available.",
        "INSUFFICIENT_STOCK" to "Some products in your cart don't have enough stock. Please update the quantities.",
        "EMPTY_ORDER" to "Your cart is empty.",
        "DUPLICATE_ORDER_ITEM" to "We couldn't process your cart. Please try again.",
        "ORDER_NOT_FOUND" to "We couldn't find this order.",
    )
}
