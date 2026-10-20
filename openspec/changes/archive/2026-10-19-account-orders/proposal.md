# Propuesta: account-orders

## Why

La pestaña Account solo tiene el selector de tema y "My orders" y el detalle de una orden son pantallas provisionales: el usuario no ve su perfil, no puede cerrar sesión ni revisar sus compras (la confirmación ya enlaza con un detalle que todavía no existe). Cerrar sesión es además el último punto que falta para que el ciclo de vida del carrito y de la sesión estén completos (Comportamiento §2 y §7.3). Es la iteración A8 de `docs/plan-iteraciones-android.md`: fusiona las iteraciones iOS `account`, `orders` y la parte de apariencia del tema.

## What Changes

- **Perfil:** nombre completo, email y plan ("Basic"/"Pro") desde `GET users/me`, recargados cada vez que la pestaña aparece; alerta genérica si falla. Un token expirado cierra la sesión (ya lo hace `ApiClient`) conservando el carrito, sin alerta.
- **Tema:** el selector System/Light/Dark (ya existía) pasa a tener su spec: cambia en vivo, se conserva entre aperturas y al cerrar sesión.
- **"My orders":** lista de órdenes (la más reciente primero) con "Order #N", fecha `Sep 15, 2026`, "1 item"/"N items" y total; estado vacío "You haven't placed any orders yet."; recarga al aparecer y al deslizar; alerta genérica si falla.
- **Detalle de orden (real):** "Order #N", estado "Confirmed", fecha y hora, pago, productos con `2 × $25.00` y subtotal, total y envío del backend; `ORDER_NOT_FOUND` → alerta "Order not found" con "OK" que vuelve a la pantalla anterior; otros errores → alerta genérica. Sustituye al detalle provisional de A7 (que ya enlaza la confirmación).
- **Cerrar sesión:** alerta "Log out?" ("Your cart will be emptied."); al confirmar vacía el carrito, termina la sesión sin aviso de expiración y reinicia la navegación para que el siguiente login abra Catalog sin pantallas apiladas.
- **Fechas del backend:** `createdAt` de la orden pasa de texto a `Instant` (0 a 9 decimales; un texto inválido falla la lectura) y `DisplayFormat` da los formatos `Sep 15, 2026` y `Sep 15, 2026, 11:30 AM`, en la zona del dispositivo.
- **IDs** `Account.*`, `Orders.*`, `OrderDetail.*` y el del botón de "Order not found" en `TestTags`; textos de las alertas en `AlertText`.

## Capabilities

### New Capabilities

- `user-account`: perfil, acceso a las órdenes y cierre de sesión con sus efectos.
- `order-history`: la lista "My orders".
- `order-detail`: el detalle de una orden y el caso de una orden inexistente o ajena.
- `app-appearance`: el tema elegido por el usuario (ya implementado en A1; aquí se formaliza y se verifica).

### Modified Capabilities

- `backend-communication`: se añaden la lectura de fechas con hora y el formato con que se muestran.

## Impact

- **Código nuevo:** `core/formatting/DisplayFormat.kt`, `features/orders/{OrdersUiState,OrdersViewModel,OrdersScreen,OrderDetailUiState,OrderDetailViewModel}.kt`, `core/network/InstantSerializer` (en `AppJson.kt`).
- **Código modificado:** `data/model/Order.kt` y `data/remote/dto/OrderDto.kt` (`createdAt: Instant`), `data/repository/{UserRepository,OrderRepository}.kt`, `features/account/{AccountUiState,AccountViewModel,AccountScreen}.kt`, `features/orders/OrderDetailScreen.kt` (deja de ser provisional), `app/{Routes,ShopApp,AppContainer,ViewModelFactories}.kt`, `core/errors/AlertText.kt`, `core/testing/TestTags.kt`, `OrderTest` (A7).
- **Tests:** unitarios (`DisplayFormatTest`, fechas en `OrderTest`) e integración (`AccountIntegrationTest`, `OrdersIntegrationTest`, backend real, **necesita `docker compose up`**).
- **Dependencias:** ninguna nueva.
- **Fuera de alcance:** extras de arranque y hooks de prueba (A9), persistir el token.
