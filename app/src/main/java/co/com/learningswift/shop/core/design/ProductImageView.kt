package co.com.learningswift.shop.core.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.com.learningswift.shop.core.testing.TestTags
import co.com.learningswift.shop.data.repository.ThemeMode

/**
 * Imagen cuadrada del producto. El ID es `product_image_<imageKey>` con la clave que se recibe, aunque se use la de respaldo.
 * Si está agotado se superpone "Sold out" sin cambiar la imagen; su ID lo pasa cada pantalla (el del catálogo y el del detalle difieren).
 */
@Composable
fun ProductImageView(imageKey: String, modifier: Modifier = Modifier, isSoldOut: Boolean = false, soldOutTag: String? = null) {
    Box(modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))) {
        Image(
            painter = painterResource(ProductImage.resourceFor(imageKey)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().testTag(TestTags.productImage(imageKey)),
        )
        if (isSoldOut) {
            val label = Modifier.align(Alignment.Center)
            SoldOutLabel(if (soldOutTag != null) label.testTag(soldOutTag) else label)
        }
    }
}

@Composable
fun SoldOutLabel(modifier: Modifier = Modifier) {
    Text(
        "Sold out",
        modifier = modifier
            .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.inverseOnSurface,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Preview(name = "Imágenes claro", showBackground = true)
@Composable
private fun ProductImagesLightPreview() = ShopTheme(ThemeMode.LIGHT) { AllImages() }

@Preview(name = "Imágenes oscuro", showBackground = true)
@Composable
private fun ProductImagesDarkPreview() = ShopTheme(ThemeMode.DARK) { AllImages() }

@Composable
private fun AllImages() {
    val keys = listOf("headphones", "keyboard", "smartwatch", "mouse", "speaker", "backpack", "unknown")
    androidx.compose.foundation.layout.Column(Modifier.padding(8.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        keys.chunked(4).forEach { row ->
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                row.forEachIndexed { index, key -> ProductImageView(key, Modifier.size(80.dp), isSoldOut = index == 2) }
            }
        }
    }
}
