package co.com.learningswift.shop.features.checkout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.com.learningswift.shop.core.design.PlaceholderScreen

/** Pantalla provisional: el checkout real llega en la iteración A7 */
@Composable
fun CheckoutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        PlaceholderScreen("Checkout")
    }
}
