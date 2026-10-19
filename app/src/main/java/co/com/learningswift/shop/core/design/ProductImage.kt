package co.com.learningswift.shop.core.design

import androidx.annotation.DrawableRes
import co.com.learningswift.shop.R

/**
 * El ÚNICO lugar que relaciona el `imageKey` del backend con su imagen. Una clave nueva se añade aquí y con su drawable
 * (generado con scripts/svg_to_vector.py). Una clave desconocida devuelve el respaldo: nunca falla ni queda vacía.
 */
object ProductImage {
    private val known = mapOf(
        "headphones" to R.drawable.product_headphones,
        "keyboard" to R.drawable.product_keyboard,
        "smartwatch" to R.drawable.product_smartwatch,
        "mouse" to R.drawable.product_mouse,
        "speaker" to R.drawable.product_speaker,
        "backpack" to R.drawable.product_backpack,
    )

    @DrawableRes
    fun resourceFor(imageKey: String): Int = known[imageKey] ?: R.drawable.product_placeholder
}
