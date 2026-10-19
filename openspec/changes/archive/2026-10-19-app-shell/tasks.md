# Tareas: app-shell

Verificación (desde la raíz del repositorio):

- **UNIT:** `./gradlew :app:testDebugUnitTest` (esta iteración no necesita el backend, pero los tests de integración de `bootstrap` sí: con él levantado corren todos).
- **BUILD:** `./gradlew :app:assembleDebug :app:lintDebug`.
- **IDs:** con la app en el emulador, `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml` y buscar el `resource-id`.

IDs a revisar en la tarea 5.2: `tab_catalog`, `tab_cart`, `tab_account`, `app_loading_indicator`, `app_snackbar_text`, `app_error_alert_retry_button`, `app_error_alert_cancel_button`.

## 1. Sesión y pestaña seleccionada

- [x] 1.1 Escribir `SessionManagerTest` (primero): `start` deja sesión sin marca de expiración; `end` la borra; `expire` deja sin sesión y con `didExpire`; `start` después de `expire` borra la marca. Verificar: UNIT pasa (la clase ya existe, el test fija el comportamiento del spec `session-management`).
- [x] 1.2 Escribir `AppContainerTest` (falla): la pestaña inicial es `Tab.Catalog` y cambiar `selectedTab.value` a `Tab.Cart` se refleja. Añadir `selectedTab` a `AppContainer`. Verificar: UNIT falla por no compilar y después pasa.
- [x] 1.3 Hacer que `MainTabs` use `selectedTab` como única fuente (design.md §1): `onClick` escribe el flujo y un `LaunchedEffect` navega. Verificar: BUILD pasa y, en el emulador, tocar las tres pestañas las muestra y las marca.

## 2. Snackbar

- [x] 2.1 Escribir `SnackbarCenterTest` (falla), con `runTest` y tiempo virtual: `show("A")` publica "A"; un segundo `show("B")` lo reemplaza; a los 3 s sin más mensajes el valor es `null`; un `show("B")` a los 2 s de "A" sigue visible a los 3 s y se borra a los 5 s. Verificar: UNIT falla por no compilar.
- [x] 2.2 Implementar `core/design/SnackbarCenter.kt` (design.md §3) y añadir `snackbar` y el `scope` opcional a `AppContainer`. Verificar: `SnackbarCenterTest` y `AppContainerTest` pasan con UNIT.
- [x] 2.3 Crear `core/design/ShopSnackbarHost.kt` con el ID `app_snackbar_text` y colocarlo en el `Scaffold` de `MainTabs`. Verificar: BUILD pasa y el mensaje aparece sobre la barra de pestañas (se prueba en 5.1).

## 3. Raíz por sesión

- [x] 3.1 Crear `app/AuthHost.kt` (design.md §4) y hacer que `ShopApp` muestre `AuthHost` o `MainTabs` según `session.state`. Verificar: BUILD pasa y al abrir la app sin sesión se ve "Login" y no hay barra de pestañas.
- [x] 3.2 Añadir en `CatalogScreen` un botón debug "End session" (temporal, solo `BuildConfig.DEBUG`) que llama `session.end()`. Verificar en el emulador: "Start test session" → pestañas; "End session" → "Login". Probar también `expire()` desde el mismo botón durante la prueba y quitar la prueba después.

## 4. Componentes comunes

- [x] 4.1 Añadir `@Preview` en claro y oscuro de `LoadingOverlay`, `GenericErrorDialog` (con `AppError.Network`) y del snackbar. Verificar: BUILD pasa y los previews se ven legibles en Android Studio.
- [x] 4.2 Con botones debug temporales en Catalog, comprobar en el emulador: el indicador existe solo mientras está activo; "Try again" cierra la alerta y ejecuta el reintento, "Cancel" solo la cierra. Quitar los botones al terminar. Verificar: spec `app-feedback`, escenarios de indicador y alerta.

## 5. Cierre

- [x] 5.1 Ejecutar la app en el emulador en claro y oscuro (`adb shell cmd uimode night yes|no`) y comprobar a la vez "Login", pestañas, snackbar (que no tape la barra), indicador y alerta. Verificar: todo es legible en las dos apariencias.
- [x] 5.2 Revisar los IDs con `uiautomator dump`: los siete de la cabecera. Si alguno no llega (sobre todo los botones del `AlertDialog`), anotarlo en `docs/catalogo-ids-accesibilidad.md` (las tres copias) y en el plan A9. Verificar: la nota existe o los siete se encuentran.
- [x] 5.3 Ejecutar UNIT, BUILD y `lintDebug`. Verificar: todo pasa y los botones debug temporales ya no están, salvo el de `AuthHost`.
- [x] 5.4 Revisar contra la spec (`app-navigation` y `app-feedback`) que cada escenario se cumple, y marcar la iteración A2 en `docs/plan-iteraciones-android.md`. Verificar: casillas marcadas.

## Seguimiento

- El responsable revisa y hace el commit; después se archiva con `/opsx:archive`.

## Resultados de la verificación

- 46 tests JVM pasan; `assembleDebug`, `assembleRelease` y `lintDebug` sin errores.
- Emulador (Pixel 6a): login ⇄ pestañas con `session.start/end/expire`; snackbar a 3 s sobre la barra; indicador existe solo mientras está activo; "Try again" suma un reintento y "Cancel" no; claro y oscuro legibles.
- IDs en `uiautomator dump`: los tres `tab_*`, `app_snackbar_text` y `app_loading_indicator` llegan. Los botones del `AlertDialog` **no** llegaban: se corrigió con `Modifier.semantics { testTagsAsResourceId = true }` en el `modifier` del diálogo y ahora llegan `app_error_alert_retry_button` y `app_error_alert_cancel_button`.
- Los botones debug de Catalog se retiraron; solo queda el de `AuthHost` hasta la A3.
