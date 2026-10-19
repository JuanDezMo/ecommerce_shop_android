# Propuesta: user-login

## Why

Tras `app-shell`, el flujo de autenticación es un texto "Login" con un botón de prueba y no hay forma real de entrar a las pestañas. Sin login no se puede validar nada contra el backend ni construir las pantallas que exigen sesión. Es la primera pantalla real de Android y la primera que habla con el backend: fija el molde (repositorio → ViewModel → pantalla) que seguirán las demás. Es la iteración A3 de `docs/plan-iteraciones-android.md`.

## What Changes

- **Pantalla de login** (email, contraseña, "Log in", "Create account") que sustituye al `AuthHost` provisional y a su botón de prueba.
- **Login contra el backend real** (`POST auth/login`): al recibir el token la sesión queda iniciada y la raíz muestra las pestañas.
- **Errores:** credenciales incorrectas y cuenta bloqueada en línea; cualquier otro error, la alerta genérica con "Try again" que repite el login.
- **Aviso de sesión expirada** cuando se llega al login por `AUTH_TOKEN_EXPIRED/INVALID/MALFORMED`.
- **Botón** habilitado solo con email y contraseña con texto (recortados) y deshabilitado mientras hay una petición, con el indicador de carga encima.
- **Error forzado solo en debug:** un email `force<400–599>@…` añade `X-Force-Error` al login, para ver la alerta genérica sin apagar el backend.
- **IDs `Login.*`** en `TestTags` (ya están en el catálogo).
- "Create account" existe pero no navega: el registro llega en A4.

Sin cambios incompatibles.

## Capabilities

### New Capabilities

- `user-login`: qué ve y qué puede hacer el usuario en el login, cuándo se habilita el botón, qué pasa con cada respuesta del backend, aviso de sesión expirada y paso a las pestañas.

### Modified Capabilities

Ninguna. El requisito "Pantalla provisional de login" de `app-navigation` (change `app-shell`, aún sin archivar) dice que existe "mientras no exista la pantalla real": este change la sustituye sin contradecirla. Al archivar, se retira ese requisito provisional de `specs/app-navigation` (nota de cierre en las tareas).

## Impact

- **Código nuevo:** `data/remote/dto/LoginRequestDto.kt` y `LoginResponseDto.kt`, `data/repository/AuthRepository.kt`, `core/testing/ForcedError.kt`, `features/login/{LoginUiState,LoginViewModel,LoginScreen}.kt`, `app/ViewModelFactories.kt`.
- **Código modificado:** `app/AppContainer.kt` (`authRepository`), `app/ShopApp.kt` y `app/AuthHost.kt` (muestra `LoginScreen`), `core/testing/TestTags.kt`.
- **Tests:** unitarios de `LoginUiState`, `ForcedError` y del `LoginViewModel`; integración `LoginIntegrationTest` contra el backend real (**necesita `docker compose up`**).
- **Dependencias:** ninguna nueva.
- **Fuera de alcance:** registro (A4), logout (A8), persistir el token, hooks de Maestro por extras de arranque (A9; aquí solo el error forzado por email).
