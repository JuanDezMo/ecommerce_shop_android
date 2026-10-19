package co.com.learningswift.shop.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.LoadingOverlay
import co.com.learningswift.shop.core.design.MessageBanner
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.errors.AppError
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.repository.ThemeMode

/** Recibe el ViewModel y los datos que necesita, no el contenedor. El dibujo está en [LoginContent] */
@Composable
fun LoginScreen(viewModel: LoginViewModel, sessionExpired: Boolean, onRegisterTap: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        sessionExpired = sessionExpired,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = { viewModel.login() },
        onRegisterTap = onRegisterTap,
        onDismissError = viewModel::dismissGenericError,
    )
}

/** Solo dibuja: no conoce al ViewModel, así se puede mostrar en un @Preview sin backend */
@Composable
fun LoginContent(
    state: LoginUiState,
    sessionExpired: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegisterTap: () -> Unit,
    onDismissError: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        // imePadding + verticalScroll: con el teclado abierto el botón sigue pudiéndose pulsar
        Column(
            Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("ShopLab", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text("Log in to continue", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))

            if (sessionExpired) {
                MessageBanner(AppError.SessionExpired.userMessage, Modifier.fillMaxWidth().testTag(TestTags.Login.SESSION_EXPIRED_TEXT))
                Spacer(Modifier.height(14.dp))
            }

            // El testTag va en el campo, no en un contenedor: si no, Maestro escribiría en el contenedor
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth().testTag(TestTags.Login.EMAIL_INPUT),
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { if (state.isLoginEnabled) onLogin() }),
                modifier = Modifier.fillMaxWidth().testTag(TestTags.Login.PASSWORD_INPUT),
            )

            state.errorMessage?.let {
                Spacer(Modifier.height(14.dp))
                MessageBanner(it, Modifier.fillMaxWidth().testTag(TestTags.Login.ERROR_TEXT), isError = true)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onLogin,
                enabled = state.isLoginEnabled,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.Login.SUBMIT_BUTTON),
            ) { Text("Log in") }
            Spacer(Modifier.height(10.dp))
            // Todavía no navega: el registro llega en la iteración A4
            OutlinedButton(
                onClick = onRegisterTap,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.Login.REGISTER_LINK),
            ) { Text("Create account") }
        }

        if (state.isLoading) LoadingOverlay()
        state.genericError?.let { GenericErrorDialog(it, onRetry = onLogin, onDismiss = onDismissError) }
    }
}

@Preview(name = "Claro", showBackground = true)
@Composable
private fun LoginLightPreview() = ShopTheme(ThemeMode.LIGHT) {
    Surface { LoginContent(LoginUiState(), false, {}, {}, {}, {}, {}) }
}

@Preview(name = "Oscuro con error", showBackground = true)
@Composable
private fun LoginDarkErrorPreview() = ShopTheme(ThemeMode.DARK) {
    Surface { LoginContent(LoginUiState("a@b.com", "x", errorMessage = "The email or password is incorrect."), false, {}, {}, {}, {}, {}) }
}

@Preview(name = "Sesión expirada", showBackground = true)
@Composable
private fun LoginExpiredPreview() = ShopTheme(ThemeMode.LIGHT) {
    Surface { LoginContent(LoginUiState(), true, {}, {}, {}, {}, {}) }
}
