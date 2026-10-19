package co.com.learningswift.shop.features.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.com.learningswift.shop.core.design.DropdownRow
import co.com.learningswift.shop.core.design.FieldErrorText
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.repository.ThemeMode
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Fecha de nacimiento: tres filas (Day, Month, Year) en una misma tarjeta, cada una con su menú.
 * El error de la fecha sale UNA sola vez, debajo de la tarjeta.
 */
@Composable
fun BirthDateField(
    day: Int,
    month: Int,
    year: Int,
    errors: List<String>?,
    onDayChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val years = BirthDateOptions.years(LocalDate.now(ZoneOffset.UTC).year)
    val days = (1..BirthDateOptions.daysInMonth(year, month)).toList()

    Column(modifier) {
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            DropdownRow("Day", day.toString(), days.map { it.toString() }, { onDayChange(days[it]) }, TestTags.Register.BIRTHDATE_DAY_DROPDOWN)
            HorizontalDivider()
            DropdownRow("Month", BirthDateOptions.months[month - 1], BirthDateOptions.months, { onMonthChange(it + 1) }, TestTags.Register.BIRTHDATE_MONTH_DROPDOWN)
            HorizontalDivider()
            DropdownRow("Year", year.toString(), years.map { it.toString() }, { onYearChange(years[it]) }, TestTags.Register.BIRTHDATE_YEAR_DROPDOWN)
        }
        FieldErrorText(errors, TestTags.Register.error(RegisterField.BIRTH_DATE.tagSuffix))
    }
}

@Preview(name = "Fecha clara", showBackground = true)
@Composable
private fun BirthDateLightPreview() = ShopTheme(ThemeMode.LIGHT) { BirthDateField(15, 9, 1990, null, {}, {}, {}) }

@Preview(name = "Fecha oscura con error", showBackground = true)
@Composable
private fun BirthDateDarkPreview() = ShopTheme(ThemeMode.DARK) {
    BirthDateField(1, 1, 2020, listOf("You must be at least 18 years old to register."), {}, {}, {})
}
