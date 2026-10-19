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
        const val CART_FULL_ALERT_OK_BUTTON = "app_cart_full_alert_ok_button"
    }

    object Login {
        const val EMAIL_INPUT = "login_email_input"
        const val PASSWORD_INPUT = "login_password_input"
        const val SUBMIT_BUTTON = "login_submit_button"
        const val REGISTER_LINK = "login_register_link"
        const val SESSION_EXPIRED_TEXT = "login_session_expired_text"
        const val ERROR_TEXT = "login_error_text"
    }

    object Register {
        const val FIRSTNAME_INPUT = "register_firstname_input"
        const val LASTNAME_INPUT = "register_lastname_input"
        const val EMAIL_INPUT = "register_email_input"
        const val PASSWORD_INPUT = "register_password_input"
        const val PHONE_INPUT = "register_phone_input"
        const val BIRTHDATE_DAY_DROPDOWN = "register_birthdate_day_dropdown"
        const val BIRTHDATE_MONTH_DROPDOWN = "register_birthdate_month_dropdown"
        const val BIRTHDATE_YEAR_DROPDOWN = "register_birthdate_year_dropdown"
        const val COUNTRY_DROPDOWN = "register_country_dropdown"
        const val PLAN_SEGMENTED = "register_plan_segmented"
        const val EXPERIENCE_SLIDER = "register_experience_slider"
        const val NEWSLETTER_SWITCH = "register_newsletter_switch"
        const val TERMS_CHECKBOX = "register_terms_checkbox"
        const val NEXT_BUTTON = "register_next_button"
        const val SUBMIT_BUTTON = "register_submit_button"
        const val ERROR_SUMMARY_TEXT = "register_error_summary_text"
        const val LOGIN_LINK = "register_login_link"
        const val SUCCESS_ALERT_LOGIN_BUTTON = "register_success_alert_login_button"

        /** Error bajo un campo: `register_<campo>_error_text` (solo existe mientras el error se ve) */
        fun error(tagSuffix: String) = "register_${tagSuffix}_error_text"
    }

    object Catalog {
        const val LIST = "catalog_list"
        const val ERROR_TEXT = "catalog_error_text"

        fun item(productId: Long) = "catalog_item_$productId"
        fun name(productId: Long) = "catalog_item_${productId}_name_text"
        fun price(productId: Long) = "catalog_item_${productId}_price_text"
        fun soldOut(productId: Long) = "catalog_item_${productId}_soldout_text"
        fun addButton(productId: Long) = "catalog_item_${productId}_add_button"
    }

    object ProductDetail {
        const val NAME_TEXT = "product_detail_name_text"
        const val DESCRIPTION_TEXT = "product_detail_description_text"
        const val PRICE_TEXT = "product_detail_price_text"
        const val STOCK_TEXT = "product_detail_stock_text"
        const val SOLD_OUT_TEXT = "product_detail_soldout_text"
        const val ADD_BUTTON = "product_detail_add_button"
    }

    object AddToCart {
        const val SHEET = "add_to_cart_sheet"
        const val NAME_TEXT = "add_to_cart_name_text"
        const val QUANTITY_STEPPER = "add_to_cart_quantity_stepper"
        const val CONFIRM_BUTTON = "add_to_cart_confirm_button"
    }

    /** `product_image_<imageKey>`: lleva la clave RECIBIDA, también cuando se muestra la imagen de respaldo */
    fun productImage(imageKey: String) = "product_image_$imageKey"

    object Account {
        const val THEME_SEGMENTED = "account_theme_segmented"
    }
}
