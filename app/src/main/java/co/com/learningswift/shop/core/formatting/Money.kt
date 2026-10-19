package co.com.learningswift.shop.core.formatting

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/** `$59.99`. Idioma fijo en_US: las pruebas de interfaz buscan siempre el mismo texto */
fun BigDecimal.usd(): String = NumberFormat.getCurrencyInstance(Locale.US).format(this)
