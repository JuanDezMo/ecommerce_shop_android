package co.com.learningswift.shop.core.formatting

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Cómo se muestran fechas, unidades y estados (Comportamiento §14). Idioma fijo en_US: las suites de interfaz buscan siempre
 * el mismo texto. La zona es un parámetro (por defecto la del dispositivo) para poder fijarla en los tests.
 */
object DisplayFormat {
    private val listPattern = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    private val detailPattern = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.US)

    /** `Sep 15, 2026` */
    fun listDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        listPattern.format(instant.atZone(zone))

    /** `Sep 15, 2026, 11:30 AM`. Las versiones recientes ponen un espacio fino (U+202F) antes de AM/PM: se cambia por uno normal */
    fun detailDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        detailPattern.format(instant.atZone(zone)).replace(' ', ' ').replace(' ', ' ')

    /** "1 item" / "3 items" */
    fun units(count: Int): String = if (count == 1) "1 item" else "$count items"

    /** `CONFIRMED` → "Confirmed" */
    fun status(raw: String): String = raw.lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
}
