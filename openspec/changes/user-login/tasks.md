# Tareas: user-login

Verificación (desde la raíz del repositorio):

- **UNIT:** `./gradlew :app:testDebugUnitTest --tests '*LoginUiStateTest' --tests '*ForcedErrorTest'` (sin red).
- **ALL:** `./gradlew :app:testDebugUnitTest`. **Requiere el backend levantado** (`docker compose up` en `../ecommerce_shop_api`, con `colima start` si hace falta; `curl http://localhost:8080/actuator/health` responde `UP`).
- **BUILD:** `./gradlew :app:assembleDebug :app:lintDebug`.
- **IDs:** `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml`.

IDs a revisar en la tarea 5.2: `login_email_input`, `login_password_input`, `login_submit_button`, `login_register_link`, `login_session_expired_text`, `login_error_text`, `app_loading_indicator`, `app_error_alert_retry_button`, `app_error_alert_cancel_button`.

## 1. Datos y repositorio

- [x] 1.1 Añadir `Login.*` a `TestTags` (los seis IDs; ya existen en el catálogo). Verificar: `TestTagsTest` pasa.
- [x] 1.2 Escribir `ForcedErrorTest` (falla): `force503@shoplab.dev` → 503; ` FORCE418@x.com ` → 418; `force200`, `force700`, `force` y `forcex` → `null`; `headersFor` devuelve `X-Force-Error` o vacío. Implementar `core/testing/ForcedError.kt` (solo con `BuildConfig.DEBUG`). Verificar: UNIT falla por no compilar y después pasa.
- [x] 1.3 Crear `LoginRequestDto`, `LoginResponseDto` y `AuthRepository.login` (design.md §1) y añadir `authRepository` a `AppContainer`. Se prueba en 2.x con el backend. Verificar: BUILD pasa.

## 2. ViewModel (test primero)

- [x] 2.1 Crear `support/MainDispatcherRule.kt` (`Dispatchers.setMain(UnconfinedTestDispatcher())`) y añadir a `TestBackend` `expiredToken(email)` (`POST test/tokens/expired`). Verificar: BUILD de tests pasa (se usan en 2.3).
- [x] 2.2 Escribir `LoginUiStateTest` (falla): `isLoginEnabled` es falso con campos vacíos, con solo espacios en cualquiera de los dos y con `isLoading`; verdadero con texto en ambos. Crear `LoginUiState`. Verificar: UNIT falla por no compilar y después pasa.
- [x] 2.3 Escribir `LoginIntegrationTest` (falla) con el `LoginViewModel` y un `ApiClient` real contra `TestBackend.BASE_URL` (**requiere backend**): `standard`/`Secret_123` inicia sesión; contraseña errónea → "The email or password is incorrect."; `locked@shoplab.dev` con `Secret_123` → "Your account is locked. Please contact support."; `locked` con contraseña errónea → credenciales incorrectas; ` Standard@ShopLab.dev ` inicia sesión; `force503@shoplab.dev` → `genericError` con el mensaje 5xx y sin mensaje en línea; `force418@shoplab.dev` → mensaje 4xx; base URL `http://localhost:9/api/v1/` → `genericError` de red; error y luego éxito borra el mensaje; un error reemplaza a otro; vaciar el email borra el mensaje y cambiarlo sin vaciar lo conserva; tras iniciar sesión el formulario queda vacío; token expirado de `expiredToken` y una ruta protegida → `session.state.didExpire`; `start` después de expirar borra la marca. Verificar: ALL falla por no compilar.
- [x] 2.4 Implementar `LoginViewModel` (design.md §2; `login()` devuelve `Job`). Verificar: `LoginIntegrationTest` pasa con ALL.

## 3. Pantalla

- [x] 3.1 Crear `LoginContent` y `LoginScreen` (design.md §3) con los IDs `Login.*`, teclados, `imePadding` y `verticalScroll`. Incluir `@Preview` en claro y oscuro: vacío, con mensaje en línea y con aviso de sesión expirada. Verificar: BUILD pasa y los previews son legibles.
- [x] 3.2 Crear `app/ViewModelFactories.kt` (mover aquí la fábrica de `AccountViewModel`), hacer que `AuthHost` muestre `LoginScreen` con `sessionExpired = session.didExpire` y quitar el botón "Start test session". Verificar: BUILD pasa y la app abre en el login.

## 4. Validación en el emulador (backend levantado)

- [x] 4.1 `standard@shoplab.dev` / `Secret_123` → pestañas, sin mensaje. Verificar en el emulador.
- [x] 4.2 Contraseña errónea y `locked@shoplab.dev` → mensajes en línea sin alerta; "Log in" vuelve a habilitarse. Verificar en el emulador.
- [x] 4.3 `force503@shoplab.dev` → alerta genérica; "Try again" repite; "Cancel" conserva los datos. Verificar en el emulador.
- [x] 4.4 Sesión expirada: con el token expirado de `POST /api/v1/test/tokens/expired` no hay forma desde la UI en esta iteración, así que se comprueba con el test 2.3 y con un `@Preview` de `LoginContent(sessionExpired = true)`. Verificar: el aviso se ve y se localiza por `login_session_expired_text`.
- [x] 4.5 Teclado: con el teclado abierto "Log in" se puede pulsar y "Next"/"Go" funcionan. Claro y oscuro (`adb shell cmd uimode night yes|no`). Verificar en el emulador.

## 5. Cierre

- [x] 5.1 Ejecutar ALL y BUILD. Verificar: todo pasa (incluidos los tests de `bootstrap` y `app-shell`).
- [x] 5.2 Revisar los IDs con `uiautomator dump` (los nueve de la cabecera). Si `login_email_input` o `login_password_input` no son `EditText`, mover el `testTag`. Verificar: los nueve se encuentran.
- [x] 5.3 Revisar contra el spec `user-login` que cada escenario se cumple, marcar la A3 en `docs/plan-iteraciones-android.md` y anotar en las tareas qué requisito provisional de `app-navigation` se retira al archivar. Verificar: casillas marcadas.

## Seguimiento

- El responsable revisa y hace el commit; después se archiva `app-shell` y luego `user-login` con `/opsx:archive`.

## Resultados de la verificación

- 69 tests JVM pasan (13 de integración de login contra el backend real y 5 de estado); `assembleDebug`, `assembleRelease` y `lintDebug` sin errores.
- Emulador (Pixel 6a, Android 17): contraseña errónea y cuenta bloqueada en línea (botón vuelve a habilitarse); `force503` abre la alerta, "Try again" la repite y "Cancel" conserva el email; vaciar o editar el email se cubre con test; `standard`/`Secret_123` con la tecla "Go" lleva a las pestañas; el botón queda visible con el teclado abierto; claro y oscuro legibles.
- IDs (`uiautomator dump`): los seis `login_*` llegan; los dos campos son `android.widget.EditText`.
- **Hallazgo:** en Android 17 (API 37) las conexiones a `10.0.2.2` agotaban el tiempo (10 s) hasta conceder `ACCESS_LOCAL_NETWORK`. Se declara solo en debug y `MainActivity` lo pide al abrir. Documentado en README, Arquitectura §11 y A9.
- Sesión expirada: cubierta por el test de `expiredToken` y el `@Preview`; en el emulador no hay forma desde la UI hasta tener logout (A8).
- Al archivar: retirar de `specs/app-navigation` el requisito provisional "Pantalla provisional de login".
