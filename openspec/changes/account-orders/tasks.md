# Tareas: account-orders

Verificación (desde la raíz del repositorio):

- **UNIT:** `./gradlew :app:testDebugUnitTest --tests '<Clase>'` (sin red).
- **ALL:** `./gradlew :app:testDebugUnitTest`. **Requiere el backend levantado** (`docker compose up` en `../ecommerce_shop_api`, perfil `testing`; `curl http://localhost:8080/actuator/health` responde `UP`). Hacen `reset` antes de cada test.
- **BUILD:** `./gradlew :app:assembleDebug :app:lintDebug`.
- **IDs:** `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml`.

IDs a revisar en la tarea 5.3: `account_name_text`, `account_email_text`, `account_plan_text`, `account_orders_button`, `account_theme_segmented`, `account_logout_button` y los dos botones de "Log out?", `orders_list`, `orders_item_<id>` y sus `_date_text`, `_total_text`, `_count_text`, `orders_empty_text`, los `order_detail_*` y `order_detail_not_found_alert_ok_button`.

Antes de empezar: `checkout` está commiteado y archivado.

## 1. Fechas y formatos (test primero)

- [x] 1.1 Escribir `DisplayFormatTest` (falla), con zona fija: `2026-09-01T03:00:00Z` da `Aug 31, 2026` y `Aug 31, 2026, 10:00 PM` en `America/Bogota`, y `Sep 1, 2026` y `Sep 1, 2026, 3:00 PM` en `Pacific/Auckland`; `units(1)` es "1 item" y `units(3)` "3 items"; `status("CONFIRMED")` es "Confirmed"; el texto con hora no contiene U+202F (antes de AM/PM hay un espacio normal). Crear `core/formatting/DisplayFormat.kt` (design.md §1). Verificar: UNIT falla por no compilar y después pasa.
- [x] 1.2 Ampliar `OrderTest` (falla): `createdAt` es un `Instant`; `2026-10-14T15:04:05Z`, `.123Z`, `.123456Z` y `.123456789Z` dan el mismo instante a nivel de milisegundos; `not-a-date` hace fallar la decodificación. Añadir `InstantSerializer` a `AppJson.kt` y pasar `createdAt` a `Instant` en `Order` y `OrderDto`. Verificar: UNIT falla y después pasa (incluidos los tests de A7 que usaban el texto).
- [x] 1.3 Ampliar `AppContainerTest` (falla): `resetNavigation()` deja `selectedTab` en `Catalog` aunque estuviera en `Account`. Añadirlo a `AppContainer`. Verificar: UNIT falla y después pasa.

## 2. Datos y ViewModels (test primero)

- [x] 2.1 Añadir a `UserRepository` `me()` y a `OrderRepository` `orders()` y `order(id)` (design.md §2), `AlertText.LogOut`, `AlertText.OrderNotFound` y los `TestTags` (`Account.*` completos, `Orders.*`, `OrderDetail.*`). Verificar: BUILD pasa y `TestTagsTest` pasa.
- [x] 2.2 Escribir `AccountIntegrationTest` (falla), con el `AccountViewModel` y backend real (**requiere backend**): `standard@shoplab.dev` → `user` con su email y plan; `logout()` → `session` sin token y sin `didExpire`, carrito vacío y `resetNavigation` llamado; token expirado (`TestBackend.expiredToken`) → `session.didExpire`, carrito conservado y **sin** `genericError`; red caída → `genericError` y, tras `loadProfile()` otra vez con backend, sin error; cambiar el tema se refleja en el estado y sobrevive a `logout()`. Ampliar `AccountUiState` y `AccountViewModel` (design.md §3) y su fábrica. Verificar: ALL falla por no compilar y después pasa.
- [x] 2.3 Escribir `OrdersIntegrationTest` (falla), con `OrdersViewModel` y `OrderDetailViewModel` y backend real (**requiere backend**): `buyer@shoplab.dev` → órdenes 2 y 1, totales `45.00` y `109.99`, unidades 1 y 3; `standard` → lista vacía; `buyer` abre la 1 (subtotales `59.99` y `50.00`, total `109.99`, envío con dirección, ciudad, código postal y país); `standard` abre la 1 (ajena) y la 999 → `notFound`; red caída → `genericError` y "Try again" repite; una compra hecha con `standard` aparece la primera en su lista. Crear `OrdersUiState`, `OrdersViewModel`, `OrderDetailUiState`, `OrderDetailViewModel` y sus fábricas. Verificar: ALL falla y después pasa.

## 3. Pantallas y navegación

- [x] 3.1 Reescribir `features/account/AccountScreen.kt` con el perfil, el selector de tema, "My orders" y "Log out" con su alerta (design.md §4); recargar el perfil con `ON_RESUME`; `@Preview` en claro, oscuro y con la alerta. Verificar: BUILD pasa.
- [x] 3.2 Crear `features/orders/OrdersScreen.kt` (lista con `PullToRefreshBox`, filas sin fusionar a los hijos, estado vacío, alerta genérica) y reescribir `OrderDetailScreen.kt` con los datos reales y la alerta "Order not found" (design.md §4); `@Preview` de la fila, el estado vacío y el detalle. Verificar: BUILD pasa.
- [x] 3.3 Conectar la navegación (design.md §5): `account_graph` con `orders` y `account_order/{id}`, el detalle de la confirmación con la pantalla real, y el logout llamando a `resetNavigation()`. Verificar: BUILD pasa.

## 4. Documentación de IDs y textos

- [x] 4.1 Comprobar que `docs/catalogo-ids-accesibilidad.md` ya tiene los IDs y textos de cuenta, órdenes y alertas (`Log out?`, `Order not found`) y anotar lo que Android añade (por ejemplo, el envío en un solo texto `order_detail_shipping_text`), en las tres copias. Verificar: `TestTagsTest` pasa.

## 5. Validación en el emulador (backend levantado, `reset` antes)

- [x] 5.1 Ejecutar ALL, BUILD y `assembleRelease`. Verificar: todo pasa, incluidos los de las iteraciones anteriores.
- [x] 5.2 En claro y oscuro:
  - **Cuenta con `standard`:** nombre completo, email y plan; el tema cambia en vivo y se conserva al cerrar y abrir la app y al cerrar sesión (el login se ve en el tema elegido);
  - **"My orders" con `standard` sin compras:** "You haven't placed any orders yet."; tras una compra (con el checkout), aparece la primera con `Order #N`, la fecha de hoy, "N items" y el total;
  - **Con `buyer@shoplab.dev`:** las órdenes 2 y 1 en ese orden; tocar la 1 abre el detalle con "Confirmed", fecha y hora, pago, las dos líneas (`× ` y subtotales), total `$109.99` y envío; atrás vuelve a la lista;
  - **"View order" desde la confirmación:** abre el detalle real y atrás vuelve a la confirmación;
  - **Orden ajena:** con `standard`, el detalle de la orden 1 (se prueba con un enlace temporal o desde un test) muestra "Order not found" y "OK" vuelve;
  - **Cerrar sesión:** con unidades en el carrito, "Log out" → "Cancel" no cambia nada; "Log out" vuelve al login sin aviso de expiración; al entrar otra vez, el carrito está vacío y la pestaña visible es Catalog (aunque se cerrara desde Account con "My orders" abierto);
  - **Token expirado:** con una sesión sembrada con el token de `POST /api/v1/test/tokens/expired`, abrir Account vuelve al login con "Your session has expired. Please log in again." y el carrito intacto;
  - **Backend apagado:** alerta genérica en Account y en "My orders"; "Try again" repite.

  Verificar: cada caso se comporta como pide `specs/user-account`, `order-history`, `order-detail` y `app-appearance`; anotar lo que difiera y corregirlo.
- [x] 5.3 Revisar los IDs con `uiautomator dump` (los de la cabecera). Si alguno no llega, anotarlo en el catálogo (las tres copias) y en el plan A9. Verificar: los IDs se encuentran o hay nota.
- [x] 5.4 Actualizar `docs/plan-iteraciones-android.md` (criterios de la A8) y `docs/shop-android-arquitectura.md` si alguna decisión cambió. Verificar: casillas marcadas.

## Seguimiento

- El responsable revisa y hace el commit; después se archiva con `/opsx:archive`.

## Resultados de la verificación

- 242 tests JVM pasan (incluidos los de integración contra el backend real); `assembleDebug`, `assembleRelease` y `lintDebug` sin errores.
- Emulador (Pixel 6a, Android 17), claro y oscuro: Account de `standard` con "Standard User", `standard@shoplab.dev` y "Basic"; el tema cambia en vivo, se conserva y el login se ve en el tema elegido tras cerrar sesión; "My orders" vacío muestra "You haven't placed any orders yet."; tras una compra aparece la primera ("Order #3", fecha de hoy, "1 item", `$59.99`); el detalle muestra "Confirmed", fecha y hora, pago, línea con `1 × $59.99`, total del backend y envío; con `buyer@shoplab.dev` salen las órdenes 2 (`$45.00`, "1 item", `Sep 15, 2026`) y 1 (`$109.99`, "3 items", `Sep 1, 2026`), y la 1 abre con subtotales `$59.99` y `$50.00`; "View order" desde la confirmación abre el detalle real y atrás vuelve; "Log out" → "Cancel" no cambia nada y "Log out" vuelve al login sin aviso de expiración, con el carrito vacío y la pestaña Catalog al volver a entrar; con el backend apagado, alerta genérica en Account y en "My orders", y deslizar para recargar lo recupera.
- IDs (`uiautomator dump`): llegan los `account_*`, los dos botones de "Log out?", `orders_list`, `orders_item_<id>` y sus `_date_text`, `_count_text`, `_total_text`, `orders_empty_text` y los `order_detail_*` (incluidos los subtotales de cada línea).
- **No probado a mano en el emulador:** el token expirado en Account y "Order not found" (necesitan inyectar un token o abrir una orden ajena, y la app no tiene todavía el extra `testSessionToken` de la A9). Ambos están cubiertos por `AccountIntegrationTest` y `OrdersIntegrationTest`.
- Hallazgo: la fila de "My orders" no estiraba el `Row` (el total quedaba pegado a la fecha); se corrigió con `fillMaxWidth()`.
