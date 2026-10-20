package co.com.learningswift.shop.features.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.com.learningswift.shop.core.design.GenericErrorDialog
import co.com.learningswift.shop.core.design.ShopTheme
import co.com.learningswift.shop.core.errors.AlertText
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.model.Plan
import co.com.learningswift.shop.data.model.User
import co.com.learningswift.shop.data.repository.ThemeMode

/** Recibe su ViewModel, lo observa y se lo pasa a un composable que solo dibuja (así se puede previsualizar) */
@Composable
fun AccountScreen(viewModel: AccountViewModel, onOrders: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Los datos se recargan cada vez que la pestaña aparece
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.loadProfile() }

    AccountContent(
        state = state,
        onThemeSelected = viewModel::onThemeSelected,
        onOrders = onOrders,
        onLogout = viewModel::logout,
        onRetry = { viewModel.loadProfile() },
        onDismissError = viewModel::dismissGenericError,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccountContent(
    state: AccountUiState,
    onThemeSelected: (ThemeMode) -> Unit,
    onOrders: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Account", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

            state.user?.let { user ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(TestTags.Account.NAME_TEXT))
                        Text(user.email, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag(TestTags.Account.EMAIL_TEXT))
                        Text(user.plan.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.testTag(TestTags.Account.PLAN_TEXT))
                    }
                }
            }

            Button(onClick = onOrders, modifier = Modifier.fillMaxWidth().testTag(TestTags.Account.ORDERS_BUTTON)) { Text("My orders") }

            Text("Theme", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag(TestTags.Account.THEME_SEGMENTED)) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = state.themeMode == mode,
                        onClick = { onThemeSelected(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                    ) { Text(mode.label) }
                }
            }

            OutlinedButton(onClick = { showLogoutDialog = true }, modifier = Modifier.fillMaxWidth().testTag(TestTags.Account.LOGOUT_BUTTON)) {
                Text("Log out", color = MaterialTheme.colorScheme.error)
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                // El diálogo es otra ventana: hay que repetir testTagsAsResourceId para que sus IDs lleguen
                modifier = Modifier.semantics { testTagsAsResourceId = true },
                title = { Text(AlertText.LogOut.TITLE) },
                text = { Text(AlertText.LogOut.MESSAGE) },
                confirmButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false; onLogout() },
                        modifier = Modifier.testTag(TestTags.Account.LOGOUT_ALERT_CONFIRM_BUTTON),
                    ) { Text(AlertText.LogOut.CONFIRM, color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }, modifier = Modifier.testTag(TestTags.Account.LOGOUT_ALERT_CANCEL_BUTTON)) {
                        Text(AlertText.LogOut.CANCEL)
                    }
                },
            )
        }
        state.genericError?.let { GenericErrorDialog(it, onRetry = onRetry, onDismiss = onDismissError) }
    }
}

private val previewUser = User(1, "standard@shoplab.dev", "Standard", "Shopper", null, "2000-01-01", "CO", Plan.BASIC, 3, false, "ACTIVE")

@Preview(name = "Cuenta clara", showBackground = true, heightDp = 640)
@Composable
private fun AccountLightPreview() = ShopTheme(ThemeMode.LIGHT) {
    AccountContent(AccountUiState(ThemeMode.LIGHT, previewUser), {}, {}, {}, {}, {})
}

@Preview(name = "Cuenta oscura", showBackground = true, heightDp = 640)
@Composable
private fun AccountDarkPreview() = ShopTheme(ThemeMode.DARK) {
    AccountContent(AccountUiState(ThemeMode.DARK, previewUser), {}, {}, {}, {}, {})
}
