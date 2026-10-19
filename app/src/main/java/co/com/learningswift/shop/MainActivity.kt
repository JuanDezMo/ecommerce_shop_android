package co.com.learningswift.shop

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import co.com.learningswift.shop.app.ShopApp

class MainActivity : ComponentActivity() {
    private val requestLocalNetwork = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askForLocalNetworkInDebug()
        val container = (application as ShopApplication).container
        setContent { ShopApp(container) }
    }

    /**
     * Android 17 bloquea las conexiones a la red local (10.0.2.2, el .local de la Mac) hasta que el usuario concede
     * este permiso, y sin él las peticiones terminan por tiempo agotado. Solo existe en debug: en release el backend es público.
     */
    private fun askForLocalNetworkInDebug() {
        if (!BuildConfig.DEBUG) return
        val granted = checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED
        if (!granted) requestLocalNetwork.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
    }
}
