package co.com.learningswift.shop.features.register

import java.time.YearMonth

/** Opciones de los tres desplegables de la fecha. Puro: sin zonas horarias ni idioma del dispositivo */
object BirthDateOptions {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    /** Del año actual a 1900, del más reciente al más antiguo */
    fun years(currentYear: Int): List<Int> = (currentYear downTo 1900).toList()

    fun daysInMonth(year: Int, month: Int): Int = YearMonth.of(year, month).lengthOfMonth()

    /** Si el día ya no existe en ese mes y año (31 → febrero), pasa al último día del mes */
    fun clampedDay(day: Int, year: Int, month: Int): Int = minOf(day, daysInMonth(year, month))
}
