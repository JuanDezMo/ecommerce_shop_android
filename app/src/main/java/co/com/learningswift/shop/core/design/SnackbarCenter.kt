package co.com.learningswift.shop.core.design

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Mensaje temporal ("Added to cart"). Uno nuevo reemplaza al actual y reinicia el tiempo:
 * por eso se cancela el temporizador anterior, para que no borre el mensaje nuevo.
 */
class SnackbarCenter(private val scope: CoroutineScope, private val durationMs: Long = 3_000) {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var timer: Job? = null

    fun show(text: String) {
        timer?.cancel()
        _message.value = text
        timer = scope.launch {
            delay(durationMs)
            _message.value = null
        }
    }

    fun clear() {
        timer?.cancel()
        _message.value = null
    }
}
