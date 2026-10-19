# Diseño: app-shell

## 1. Pestaña seleccionada: una sola fuente de verdad

`AppContainer.selectedTab: MutableStateFlow<Tab>` (inicial `Tab.Catalog`). `MainTabs` la lee con `collectAsStateWithLifecycle`; la barra marca como seleccionada la que indica el flujo, y `onClick` solo escribe `selectedTab.value = tab`. Un `LaunchedEffect(selectedTab)` navega con el `NavController` (con `launchSingleTop` y `restoreState`). Así, "Done" del checkout (A7) solo hace `selectedTab.value = Tab.Catalog`.

Alternativa descartada: dejar la ruta actual del `NavController` como fuente y exponerla a las pantallas; obliga a que las pantallas conozcan el `NavController`.

## 2. Raíz

`ShopApp` observa `container.session.state`; `if (state.isLoggedIn) MainTabs(...) else AuthHost(...)`. Sin animación en esta iteración. El tema se sigue aplicando por encima de ambos.

## 3. Snackbar

`SnackbarCenter(scope: CoroutineScope, durationMs: Long = 3_000)` con `message: StateFlow<String?>`. `show(text)` cancela el `Job` anterior, publica el texto y lanza un `Job` que espera `durationMs` y limpia. Cancelar el `Job` anterior garantiza que el mensaje viejo no borre al nuevo (escenario "El mensaje viejo no apaga al nuevo").

No se usa `SnackbarHostState` de Material: no deja fijar 3 s exactos ni reemplazar sin cola.

`AppContainer` recibe un `scope: CoroutineScope` (por defecto `CoroutineScope(SupervisorJob() + Dispatchers.Main)`) para no atar el centro de mensajes al ciclo de una pantalla; los tests pasan el `TestScope`.

`ShopSnackbarHost` se dibuja en el `snackbarHost` del `Scaffold` de las pestañas, que ya lo coloca sobre la barra inferior. El `Text` lleva `testTag(app_snackbar_text)` y solo existe mientras hay mensaje.

## 4. AuthHost provisional

Columna centrada con `Text("Login")` y, solo si `BuildConfig.DEBUG`, un botón "Start test session" que llama `session.start("debug-token")`. El token es falso: las pestañas de esta iteración no llaman al backend. Se retira en A3.

## 5. Componentes ya existentes

`LoadingOverlay` y `GenericErrorDialog` nacieron en `bootstrap`. Aquí se les añade `@Preview` (claro y oscuro) y se verifican en emulador con un botón debug temporal en Catalog que enciende cada uno; el botón se quita al cerrar la iteración.

## 6. Verificación

- Unitarios JVM: `SnackbarCenterTest` (con `runTest` y tiempo virtual), `SessionManagerTest`, `AppContainerTest`.
- Emulador: iniciar y cerrar sesión cambia entre "Login" y pestañas; snackbar sobre la barra en claro y oscuro; IDs con `adb shell uiautomator dump` (`resource-id`).
