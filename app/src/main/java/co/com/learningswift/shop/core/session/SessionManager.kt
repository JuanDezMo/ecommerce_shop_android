package co.com.learningswift.shop.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SessionState(val token: String? = null, val didExpire: Boolean = false) {
    val isLoggedIn: Boolean get() = token != null
}

/** Estado de la sesión. El token vive solo en memoria (fase 1): al cerrar la app hay que iniciar sesión otra vez */
class SessionManager {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val token: String? get() = _state.value.token

    fun start(token: String) {
        _state.value = SessionState(token = token)
    }

    /** Logout o AUTH_TOKEN_MISSING: sin aviso de sesión expirada */
    fun end() {
        _state.value = SessionState()
    }

    /** AUTH_TOKEN_EXPIRED, _INVALID o _MALFORMED: el login muestra el aviso */
    fun expire() {
        _state.value = SessionState(didExpire = true)
    }
}
