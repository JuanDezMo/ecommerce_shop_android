package co.com.learningswift.shop.features.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.DropdownRow
import co.com.learningswift.shop.core.design.ErrorSummary
import co.com.learningswift.shop.core.design.FieldErrorText
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.LabeledCheckbox
import co.com.learningswift.shop.core.design.LoadingOverlay
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.Plan
import co.com.learningswift.shop.data.repository.ThemeMode

/**
 * Paso 2 de 2: el resto de los datos y el envío.
 * `onNeedsBasics`: el backend rechazó un campo del paso 1 (por ejemplo, un email repetido) y hay que volver a él.
 * `onRegistered`: la cuenta se creó; recibe el email para volver al login con él escrito.
 */
@Composable
fun RegisterDetailsScreen(viewModel: RegisterViewModel, onNeedsBasics: () -> Unit, onRegistered: (email: String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.scrollTick) {
        if (state.scrollTick > 0 && state.stepOfFirstError == RegisterStep.BASICS) onNeedsBasics()
    }

    RegisterDetailsContent(
        state = state,
        actions = RegisterActions.from(viewModel),
        onSubmit = { viewModel.submit() },
        onAccountCreatedLogin = { email ->
            viewModel.consumeRegisteredEmail()
            onRegistered(email)
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterDetailsContent(
    state: RegisterUiState,
    actions: RegisterActions,
    onSubmit: () -> Unit,
    onAccountCreatedLogin: (email: String) -> Unit,
) {
    val form = state.form
    val errors = state.fieldErrors
    val requesters = remember { RegisterField.entries.associateWith { BringIntoViewRequester() } }

    LaunchedEffect(state.scrollTick) {
        RegisterField.entries.firstOrNull { it.step == RegisterStep.DETAILS && it in errors }?.let { requesters.getValue(it).bringIntoView() }
    }

    fun field(f: RegisterField) = Modifier.bringIntoViewRequester(requesters.getValue(f))
    fun errorTag(f: RegisterField) = TestTags.Register.error(f.tagSuffix)

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().systemBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Step 2 of 2", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("About you", style = MaterialTheme.typography.headlineMedium)
                ErrorSummary(state.errorCount(RegisterStep.DETAILS), TestTags.Register.ERROR_SUMMARY_TEXT)

                Text("Date of birth", style = MaterialTheme.typography.titleSmall)
                BirthDateField(
                    form.birthDay, form.birthMonth, form.birthYear, errors[RegisterField.BIRTH_DATE],
                    actions.onBirthDayChange, actions.onBirthMonthChange, actions.onBirthYearChange,
                    field(RegisterField.BIRTH_DATE),
                )

                Column(field(RegisterField.COUNTRY)) {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                        val countries = Country.sorted
                        DropdownRow(
                            "Country", form.country?.displayName ?: "Select",
                            listOf("Select") + countries.map { it.displayName },
                            { index -> actions.onCountryChange(if (index == 0) null else countries[index - 1]) },
                            TestTags.Register.COUNTRY_DROPDOWN,
                        )
                    }
                    FieldErrorText(errors[RegisterField.COUNTRY], errorTag(RegisterField.COUNTRY))
                }

                Column(field(RegisterField.PLAN)) {
                    Text("Plan", style = MaterialTheme.typography.titleSmall)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag(TestTags.Register.PLAN_SEGMENTED)) {
                        Plan.entries.forEachIndexed { index, plan ->
                            SegmentedButton(
                                selected = form.plan == plan,
                                onClick = { actions.onPlanChange(plan) },
                                shape = SegmentedButtonDefaults.itemShape(index, Plan.entries.size),
                            ) { Text(plan.label) }
                        }
                    }
                    FieldErrorText(errors[RegisterField.PLAN], errorTag(RegisterField.PLAN))
                }

                Column(field(RegisterField.EXPERIENCE)) {
                    Text("Experience level: ${form.experienceLevel}", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = form.experienceLevel.toFloat(),
                        onValueChange = { actions.onExperienceChange(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,   // 1, 2, 3, 4, 5: solo enteros
                        modifier = Modifier.testTag(TestTags.Register.EXPERIENCE_SLIDER),
                    )
                    FieldErrorText(errors[RegisterField.EXPERIENCE], errorTag(RegisterField.EXPERIENCE))
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subscribe to the newsletter", style = MaterialTheme.typography.bodyLarge)
                    Switch(form.newsletter, actions.onNewsletterChange, Modifier.testTag(TestTags.Register.NEWSLETTER_SWITCH))
                }

                Column(field(RegisterField.TERMS)) {
                    LabeledCheckbox(form.termsAccepted, actions.onTermsChange, "I accept the terms and conditions", TestTags.Register.TERMS_CHECKBOX)
                    FieldErrorText(errors[RegisterField.TERMS], errorTag(RegisterField.TERMS))
                }

                Spacer(Modifier.height(4.dp))
                Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth().testTag(TestTags.Register.SUBMIT_BUTTON)) { Text("Create account") }
            }

            if (state.isLoading) LoadingOverlay()
            state.genericError?.let { GenericErrorDialog(it, onRetry = onSubmit, onDismiss = actions.onDismissGenericError) }
            state.registeredEmail?.let { email ->
                AlertDialog(
                    onDismissRequest = {},   // la única salida es "Log in"
                    // El diálogo es otra ventana: hay que repetir testTagsAsResourceId para que sus IDs lleguen
                    modifier = Modifier.semantics { testTagsAsResourceId = true },
                    title = { Text(AlertText.AccountCreated.TITLE) },
                    text = { Text(AlertText.AccountCreated.MESSAGE) },
                    confirmButton = {
                        TextButton(onClick = { onAccountCreatedLogin(email) }, modifier = Modifier.testTag(TestTags.Register.SUCCESS_ALERT_LOGIN_BUTTON)) {
                            Text(AlertText.AccountCreated.LOGIN)
                        }
                    },
                )
            }
        }
    }
}

@Preview(name = "Paso 2 claro", showBackground = true, heightDp = 1100)
@Composable
private fun DetailsLightPreview() = ShopTheme(ThemeMode.LIGHT) { RegisterDetailsContent(RegisterUiState(), RegisterActions(), {}, {}) }

@Preview(name = "Paso 2 oscuro con errores", showBackground = true, heightDp = 1100)
@Composable
private fun DetailsDarkErrorsPreview() = ShopTheme(ThemeMode.DARK) {
    val errors = mapOf(
        RegisterField.BIRTH_DATE to listOf("You must be at least 18 years old to register."),
        RegisterField.COUNTRY to listOf("Select your country."),
        RegisterField.TERMS to listOf("You must accept the terms and conditions."),
    )
    RegisterDetailsContent(RegisterUiState(fieldErrors = errors), RegisterActions(), {}, {})
}
