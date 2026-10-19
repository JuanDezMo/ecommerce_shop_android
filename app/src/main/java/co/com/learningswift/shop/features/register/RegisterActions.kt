package co.com.learningswift.shop.features.register

import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.Plan

/**
 * Lo que las pantallas del registro pueden pedir. Agrupa las funciones del ViewModel para que las pantallas
 * "que solo dibujan" (RegisterBasicContent/RegisterDetailsContent) no dependan de él y se puedan previsualizar.
 */
data class RegisterActions(
    val onFirstNameChange: (String) -> Unit = {},
    val onLastNameChange: (String) -> Unit = {},
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onPhoneChange: (String) -> Unit = {},
    val onBirthDayChange: (Int) -> Unit = {},
    val onBirthMonthChange: (Int) -> Unit = {},
    val onBirthYearChange: (Int) -> Unit = {},
    val onCountryChange: (Country?) -> Unit = {},
    val onPlanChange: (Plan) -> Unit = {},
    val onExperienceChange: (Int) -> Unit = {},
    val onNewsletterChange: (Boolean) -> Unit = {},
    val onTermsChange: (Boolean) -> Unit = {},
    val onFieldBlur: (RegisterField) -> Unit = {},
    val onDismissGenericError: () -> Unit = {},
) {
    companion object {
        fun from(viewModel: RegisterViewModel) = RegisterActions(
            onFirstNameChange = viewModel::onFirstNameChange,
            onLastNameChange = viewModel::onLastNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onPhoneChange = viewModel::onPhoneChange,
            onBirthDayChange = viewModel::onBirthDayChange,
            onBirthMonthChange = viewModel::onBirthMonthChange,
            onBirthYearChange = viewModel::onBirthYearChange,
            onCountryChange = viewModel::onCountryChange,
            onPlanChange = viewModel::onPlanChange,
            onExperienceChange = viewModel::onExperienceChange,
            onNewsletterChange = viewModel::onNewsletterChange,
            onTermsChange = viewModel::onTermsChange,
            onFieldBlur = viewModel::onFieldBlur,
            onDismissGenericError = viewModel::dismissGenericError,
        )
    }
}
