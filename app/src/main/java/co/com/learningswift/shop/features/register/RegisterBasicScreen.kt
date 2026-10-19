package co.com.learningswift.shop.features.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.ErrorSummary
import co.com.learningswift.shop.core.design.FormTextField
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.repository.ThemeMode

/** Paso 1 de 2: datos básicos. Recibe el ViewModel compartido con el paso 2 (no lo crea) */
@Composable
fun RegisterBasicScreen(viewModel: RegisterViewModel, onNext: () -> Unit, onLoginTap: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RegisterBasicContent(
        state = state,
        actions = RegisterActions.from(viewModel),
        onNext = { if (viewModel.continueToDetails()) onNext() },
        onLoginTap = onLoginTap,
    )
}

/** Solo dibuja: así se puede mostrar en un @Preview sin backend */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterBasicContent(state: RegisterUiState, actions: RegisterActions, onNext: () -> Unit, onLoginTap: () -> Unit) {
    val form = state.form
    val errors = state.fieldErrors
    val focusManager = LocalFocusManager.current
    val requesters = remember { RegisterField.entries.associateWith { BringIntoViewRequester() } }

    // Tras un intento con errores (y al volver del paso 2 por un error del backend), se desplaza al primero de ESTE paso
    LaunchedEffect(state.scrollTick) {
        RegisterField.entries.firstOrNull { it.step == RegisterStep.BASICS && it in errors }?.let { requesters.getValue(it).bringIntoView() }
    }

    fun field(f: RegisterField) = Modifier.bringIntoViewRequester(requesters.getValue(f))
    fun errorTag(f: RegisterField) = TestTags.Register.error(f.tagSuffix)

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().systemBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Step 1 of 2", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Your details", style = MaterialTheme.typography.headlineMedium)
                ErrorSummary(state.errorCount(RegisterStep.BASICS), TestTags.Register.ERROR_SUMMARY_TEXT)

                FormTextField(
                    "First name", form.firstName, actions.onFirstNameChange, TestTags.Register.FIRSTNAME_INPUT,
                    errorTag(RegisterField.FIRST_NAME), errors[RegisterField.FIRST_NAME], field(RegisterField.FIRST_NAME),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onBlur = { actions.onFieldBlur(RegisterField.FIRST_NAME) },
                )
                FormTextField(
                    "Last name", form.lastName, actions.onLastNameChange, TestTags.Register.LASTNAME_INPUT,
                    errorTag(RegisterField.LAST_NAME), errors[RegisterField.LAST_NAME], field(RegisterField.LAST_NAME),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onBlur = { actions.onFieldBlur(RegisterField.LAST_NAME) },
                )
                FormTextField(
                    "Email", form.email, actions.onEmailChange, TestTags.Register.EMAIL_INPUT,
                    errorTag(RegisterField.EMAIL), errors[RegisterField.EMAIL], field(RegisterField.EMAIL),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
                    onBlur = { actions.onFieldBlur(RegisterField.EMAIL) },
                )
                FormTextField(
                    "Password", form.password, actions.onPasswordChange, TestTags.Register.PASSWORD_INPUT,
                    errorTag(RegisterField.PASSWORD), errors[RegisterField.PASSWORD], field(RegisterField.PASSWORD),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    visualTransformation = PasswordVisualTransformation(),
                    onBlur = { actions.onFieldBlur(RegisterField.PASSWORD) },
                )
                FormTextField(
                    "Phone", form.phone, actions.onPhoneChange, TestTags.Register.PHONE_INPUT,
                    errorTag(RegisterField.PHONE), errors[RegisterField.PHONE], field(RegisterField.PHONE),
                    // El teclado numérico no tiene "Next": su ✓ cierra el teclado y deja el botón "Next" a la vista
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    supportingText = "For example +5733001234567",
                    onBlur = { actions.onFieldBlur(RegisterField.PHONE) },
                )

                Spacer(Modifier.height(4.dp))
                // Siempre habilitado: así cada error se puede provocar y comprobar
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth().testTag(TestTags.Register.NEXT_BUTTON)) { Text("Next") }
                TextButton(onClick = onLoginTap, modifier = Modifier.fillMaxWidth().testTag(TestTags.Register.LOGIN_LINK)) {
                    Text("Already have an account? Log in")
                }
            }
        }
    }
}

@Preview(name = "Paso 1 claro", showBackground = true, heightDp = 900)
@Composable
private fun BasicLightPreview() = ShopTheme(ThemeMode.LIGHT) { RegisterBasicContent(RegisterUiState(), RegisterActions(), {}, {}) }

@Preview(name = "Paso 1 oscuro con errores", showBackground = true, heightDp = 900)
@Composable
private fun BasicDarkErrorsPreview() = ShopTheme(ThemeMode.DARK) {
    val errors = RegisterField.entries.filter { it.step == RegisterStep.BASICS }.associateWith { listOf("Enter this field.") }
    RegisterBasicContent(RegisterUiState(fieldErrors = errors), RegisterActions(), {}, {})
}
