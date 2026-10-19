# Diseño: user-login

## Contexto

Ya existe y se reutiliza: `ApiClient.post(path, body, authorized, headers)` (convierte los fallos en `AppError`), `SessionManager`, `ErrorMessages` (`INVALID_CREDENTIALS`, `USER_LOCKED`, sesión expirada), `AlertText`, `GenericErrorDialog`, `LoadingOverlay`, `MessageBanner` y, de `app-shell` (sin archivar), `AuthHost` y la raíz por sesión.

Backend (guía de integración): `POST auth/login` con `{email, password}` → 200 `{accessToken, tokenType, expiresIn}`; 401 `INVALID_CREDENTIALS`; 423 `USER_LOCKED` (solo con la contraseña correcta); 400 si un campo está en blanco. El backend ya recorta y pasa a minúsculas el email y no valida el formato de la contraseña.

Código de referencia: Arquitectura §5.6 a §5.8.

## Decisiones

### 1. DTOs y repositorio

```kotlin
@Serializable data class LoginRequestDto(val email: String, val password: String)
@Serializable data class LoginResponseDto(val accessToken: String, val tokenType: String, val expiresIn: Int)

class AuthRepository(private val api: ApiClient, private val session: SessionManager) {
    suspend fun login(email: String, password: String) {
        val headers = ForcedError.headersFor(email)
        val response: LoginResponseDto = api.post("auth/login", LoginRequestDto(email.trim(), password), headers = headers)
        session.start(response.accessToken)
    }
}
```

- Recorta el email (lo exige el spec), no toca la contraseña y no pasa a minúsculas (lo hace el backend: una sola regla en un solo sitio).
- No devuelve el token: lo entrega a `SessionManager`; los ViewModels nunca lo ven.
- `expiresIn` se lee pero no se usa: el token vive en memoria y el backend avisa con un 401.

### 2. `LoginUiState` y `LoginViewModel`

Es el patrón de Arquitectura §5.7, con tres precisiones:

- `isLoginEnabled` usa `isNotBlank()` en ambos campos y `!isLoading`.
- `login()` empieza con `if (!state.isLoginEnabled) return` (el botón no es la única defensa contra el doble envío) y **devuelve el `Job`** de la corrutina. Es para que los tests esperen el resultado con `join()`; la pantalla lo ignora.
- Solo `INVALID_CREDENTIALS` y `USER_LOCKED` van en línea; el resto de `AppError` va a `genericError`. Se atrapa `AppError`, nunca `Exception`: así la cancelación de la corrutina no se traga.
- **El formulario se vacía al iniciar sesión con éxito** (`_state.value = LoginUiState()`), porque el `LoginViewModel` pertenece a la actividad y sobrevive al logout: sin esto, tras cerrar sesión el login mostraría el email y la contraseña anteriores. En iOS se resolvía creando un ViewModel nuevo; en Android el vaciado explícito es más sencillo y se cubre con un test.
- Vaciar el email (en blanco) borra el mensaje en línea; cambiarlo sin vaciarlo lo conserva (spec).

### 3. `LoginScreen`

- `LoginScreen(viewModel, sessionExpired, onRegisterTap)` recoge el estado con `collectAsStateWithLifecycle` y delega el dibujo en `LoginContent(state, …)` sin ViewModel, para poder previsualizarlo (claro y oscuro, con y sin mensaje).
- `Column` con `imePadding()` y `verticalScroll`: el botón se puede pulsar con el teclado abierto y en pantallas pequeñas.
- Cabecera "ShopLab" / "Log in to continue"; aviso de sesión expirada (`MessageBanner`, ID `login_session_expired_text`); `OutlinedTextField` de email (`KeyboardType.Email`, sin mayúscula, `ImeAction.Next`) y de contraseña (`PasswordVisualTransformation`, `ImeAction.Go` que llama a `login()`); mensaje en línea (`MessageBanner` de error, `login_error_text`); "Log in" (botón principal, ancho completo) y "Create account" (`OutlinedButton`, `onRegisterTap = {}` con un comentario que apunta a A4).
- **El `testTag` va en el `OutlinedTextField`**, no en un contenedor (si no, Maestro escribe en el contenedor).
- `LoadingOverlay` encima cuando `isLoading`; `GenericErrorDialog` con `onRetry = viewModel::login`.

### 4. Raíz y fábricas

`ViewModelFactories.kt` agrupa las fábricas de ViewModel (la de `AccountViewModel` pasa también aquí). `AuthHost` recibe el `container`, obtiene el `LoginViewModel` con su fábrica y pasa `sessionExpired = session.didExpire`. **Se retira el botón "Start test session"**: desde ahora se entra con el login real.

### 5. Error forzado (solo debug)

```kotlin
object ForcedError {
    fun status(email: String): Int?        // "force503@x" → 503; fuera de 400..599 o sin forma → null
    fun headersFor(email: String): Map<String, String>   // {"X-Force-Error": "503"} o vacío
}
```

- Forma: parte local `force` + número de 400 a 599; ignora mayúsculas y espacios.
- Con `BuildConfig.DEBUG == false` siempre devuelve `null`/vacío: en release el comportamiento no existe.
- No es un mock: la petición sale al backend real, que responde el error (`FORCED_ERROR`). Requiere el perfil `testing`.
- Se adelanta a esta iteración solo para el login; los hooks generales (`testForceError` por extra de arranque) llegan en A9 y reutilizan el parámetro `headers` de `ApiClient`.

### 6. Pruebas

- **Unitarias sin red:** `LoginUiStateTest` (campos vacíos, solo espacios en cada campo, ambos con texto, `isLoading`), `ForcedErrorTest` (`force503`, `FORCE418 ` con mayúsculas/espacios, `force200`, `force700`, `force`, `forcex`).
- **Integración (`LoginIntegrationTest`, backend real, reset antes de cada test):** usuarios semilla `standard@shoplab.dev` y `locked@shoplab.dev`, contraseña incorrecta, email con espacios y mayúsculas, `force503`/`force418`, base URL sin servidor (`http://localhost:9/api/v1/`) → `genericError` sin mensaje en línea, vaciado del formulario al iniciar sesión, token expirado (`POST test/tokens/expired`) → `session.expire()`.
- Los `ViewModel` usan `viewModelScope` (Main): los tests ponen `Dispatchers.setMain(UnconfinedTestDispatcher())` con una regla `MainDispatcherRule` en `support/`.
- La parte visual se ve en el emulador contra el backend real y con previews: no hay tests de UI en esta fase.

## Riesgos

- **Backend caído:** los tests de integración y la validación visual necesitan `docker compose up` (con Colima).
- **Emulador → backend:** `10.0.2.2:8080`; si falla, revisar `network_security_config`.
- **Teclado sobre el botón:** `imePadding` + `verticalScroll`; se comprueba en el emulador.
- **El `testTag` en un contenedor:** se revisa con `uiautomator dump` que `login_email_input` y `login_password_input` son campos editables (`class=android.widget.EditText`).
- **El aviso de sesión expirada depende de `app-shell`** (sin archivar): se reconcilia al archivar.
