package co.com.learningswift.shop.core.testing

/**
 * Copia de docs/catalogo-ids-accesibilidad.md. Un ID nuevo se añade PRIMERO al catálogo y después aquí;
 * una prueba unitaria comprueba que todo ID de este objeto exista en el catálogo.
 */
object TestTags {
    object App {
        const val LOADING_INDICATOR = "app_loading_indicator"
        const val SNACKBAR_TEXT = "app_snackbar_text"
        const val TAB_CATALOG = "tab_catalog"
        const val TAB_CART = "tab_cart"
        const val TAB_ACCOUNT = "tab_account"
        const val ERROR_ALERT_RETRY_BUTTON = "app_error_alert_retry_button"
        const val ERROR_ALERT_CANCEL_BUTTON = "app_error_alert_cancel_button"
    }

    object Login {
        const val EMAIL_INPUT = "login_email_input"
        const val PASSWORD_INPUT = "login_password_input"
        const val SUBMIT_BUTTON = "login_submit_button"
        const val REGISTER_LINK = "login_register_link"
        const val SESSION_EXPIRED_TEXT = "login_session_expired_text"
        const val ERROR_TEXT = "login_error_text"
    }

    object Account {
        const val THEME_SEGMENTED = "account_theme_segmented"
    }
}
