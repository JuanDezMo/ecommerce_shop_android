# Propuesta: app-shell

## Why

Tras `bootstrap`, la raíz siempre muestra las pestañas: no decide según la sesión, no hay forma de que una pantalla cambie de pestaña ni existe el mensaje temporal (snackbar) que usarán el carrito y otras pantallas. Hacerlo ahora, una sola vez, evita que cada iteración invente su versión y rompa el contrato de IDs compartido con iOS. Es la iteración A2 de `docs/plan-iteraciones-android.md`.

## What Changes

- **Raíz por sesión:** `ShopApp` muestra `AuthHost` (provisional, título "Login") sin sesión y las pestañas con sesión, y reacciona en cuanto cambia `session.state`. En debug, `AuthHost` tiene un botón provisional para iniciar una sesión de prueba y la pestaña Catalog uno para cerrarla; ambos se retiran en la iteración de login.
- **Pestaña controlable:** `AppContainer.selectedTab` (`StateFlow<Tab>`) es la única fuente de verdad; tocar la barra y pedir el cambio desde una pantalla usan el mismo camino.
- **Snackbar:** `SnackbarCenter` (3 s, uno nuevo reemplaza y reinicia el tiempo) y `ShopSnackbarHost` (`app_snackbar_text`), sobre la barra de pestañas.
- **Componentes comunes ya existentes** (`LoadingOverlay`, `GenericErrorDialog`): se les añade un `@Preview` en claro y oscuro y se verifican contra la spec.
- **Tests:** `SnackbarCenterTest` (tiempo virtual), `SessionManagerTest` y `AppContainerTest` (pestaña inicial y cambio).

## Capabilities

### New Capabilities

- `app-navigation`: qué ve el usuario al abrir la app según la sesión, las tres pestañas con sus IDs y la pestaña seleccionable por la app.
- `app-feedback`: indicador de carga, snackbar temporal y alerta genérica con "Try again" y "Cancel".

### Modified Capabilities

Ninguna.

## Impact

- **Código nuevo:** `core/design/SnackbarCenter.kt`, `core/design/ShopSnackbarHost.kt`, `app/AuthHost.kt`.
- **Código modificado:** `app/ShopApp.kt`, `app/AppContainer.kt` (añade `snackbar`, `selectedTab` y un `CoroutineScope` opcional), `core/design/Components.kt` (previews), `features/catalog/CatalogScreen.kt` (botón debug).
- **Tests:** JVM, sin backend. Verificación manual en emulador (claro y oscuro).
- **Fuera de alcance:** login real, pila de navegación por pestaña (llega con la primera pantalla que la necesita), insignia del carrito.
