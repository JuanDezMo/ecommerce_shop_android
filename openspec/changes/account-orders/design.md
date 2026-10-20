# Diseño: account-orders

## Contexto

Tras `checkout`: `AccountScreen` con el selector de tema (`SettingsRepository`, `ThemeMode`), `OrderRepository.createOrder`, `Order`/`OrderItem`/`Shipping` (con `createdAt` como texto), `OrderDetailScreen` y `ConfirmationScreen` provisionales/reales, `CartRepository.clear()`, `SessionManager.end()/expire()`, `ApiClient` (que ya cierra la sesión ante un 401 de token) y los grafos por pestaña (`account_graph` solo tiene `account`).

Backend: `GET users/me` (nombre, email, plan…), `GET orders` (de la más reciente a la más antigua, con `items`), `GET orders/{id}` (404 `ORDER_NOT_FOUND` también si es de otro usuario). Usuarios semilla: `buyer@shoplab.dev` tiene las órdenes 2 (`45.00`, 1 unidad) y 1 (`109.99`, 3 unidades, subtotales `59.99` y `50.00`); `standard` no tiene ninguna. Referencias: Comportamiento §2, §10 y §14; diseños de iOS `account` y `orders`.

## Decisiones

### 1. Fechas

- `createdAt` pasa a **`Instant`**. `InstantSerializer` en `AppJson.kt` usa `Instant.parse`, que acepta 0 a 9 decimales y la `Z` de UTC y **lanza si el texto no es una fecha**: `ApiClient.decode` lo convierte en `AppError.Unknown` (la spec: un texto inválido hace fallar la lectura de esa respuesta).
- `DisplayFormat` (`core/formatting`): `listDate(instant, zone)` → `Sep 15, 2026` (`MMM d, yyyy`, `Locale.US`), `detailDate(instant, zone)` → `Sep 15, 2026, 11:30 AM` (`MMM d, yyyy, h:mm a`), `units(n)` → "1 item"/"N items" y `status("CONFIRMED")` → "Confirmed". **La zona es un parámetro** (por defecto `ZoneId.systemDefault()`): los tests la fijan. En las versiones recientes el formato de AM/PM usa un espacio fino sin separación (U+202F) antes de AM/PM: se sustituye por un espacio normal para que las suites busquen siempre el mismo texto.

### 2. Repositorios

`UserRepository.me(): User` (`GET users/me`, con token; reutiliza `UserDto`) y `OrderRepository.orders(): List<Order>` y `order(id): Order` (con token). `UserDto` ya ignora los campos que no usa.

### 3. Cuenta, perfil y cierre de sesión

`AccountUiState`: `themeMode`, `user: User?`, `isLoading`, `genericError`. `AccountViewModel(settings, userRepository, cart, session, resetNavigation: () -> Unit)`:

- `loadProfile(): Job` (la pantalla lo llama con `ON_RESUME`): `me()` → `user`. Si falla con `AppError.SessionExpired` o `NotAuthenticated`, **no se muestra nada**: `ApiClient` ya terminó la sesión y la raíz vuelve al login (con el aviso si fue expiración); el carrito no se toca. Cualquier otro error → `genericError` y "Try again" repite la carga.
- `logout()`: `cart.clear()`, `session.end()` (sin marca de expiración) y `resetNavigation()`. Con sesión expirada NO se llama (no hay botón: la raíz ya cambió).
- El tema (`onThemeSelected`) no cambia: `SettingsRepository` ya lo guarda y `ShopApp` lo observa, también en el login.

`AppContainer.resetNavigation()`: `selectedTab.value = Tab.Catalog`. Las pilas de cada pestaña viven en el `NavController` de `MainTabs`, que sale de la composición al terminar la sesión y se crea nuevo al volver a entrar; lo único que sobrevivía era `selectedTab`, que por eso se reinicia aquí.

### 4. Lista de órdenes y detalle

`OrdersViewModel(orderRepository)`: `OrdersUiState(orders, isLoading, genericError)`; `load()` con `ON_RESUME`; el indicador a pantalla completa solo en la primera carga (como el catálogo). `OrderDetailViewModel(orderRepository, id)`: `OrderDetailUiState(order, isLoading, notFound, genericError)`: `ORDER_NOT_FOUND` → `notFound = true` (alerta "Order not found" con "OK" que llama a `onBack`); otros → `genericError` con "Try again".

**Pantallas:** `OrdersScreen` (`LazyColumn` `orders_list` con `PullToRefreshBox`; fila `orders_item_<id>` sin fusionar a los hijos, con fecha, unidades y total; `orders_empty_text`), `OrderDetailScreen` real con cabecera, líneas `order_detail_item_<productId>` (`2 × $25.00` y subtotal), total y envío (cuatro líneas en un solo texto `order_detail_shipping_text`). `AccountScreen` suma el perfil (`account_name_text`, `account_email_text`, `account_plan_text`), "My orders" y "Log out" con su `AlertDialog` ("Log out?", "Your cart will be emptied.", "Log out" destructivo y "Cancel", con `testTagsAsResourceId`).

### 5. Navegación

- `account_graph`: `account` → `orders` → `account_order/{id}`.
- `cart_graph` mantiene `order/{id}` (desde la confirmación). Las **rutas son distintas** (`order/{id}` y `account_order/{id}`) para no tener dos destinos con la misma ruta en grafos distintos; las dos muestran `OrderDetailScreen`.
- "OK" de "Order not found" y la flecha hacen `popBackStack()`: vuelven a la lista o a la confirmación según de dónde se vino.

### 6. Pruebas

- **Unitarias sin red:** `DisplayFormatTest` (zona `America/Bogota` y `Pacific/Auckland`, que cambian de día; `1 item`/`3 items`; "Confirmed"; espacio normal antes de AM/PM), `OrderTest` ampliado (fechas con 0, 3, 6 y 9 decimales → mismo instante a nivel de milisegundos, `not-a-date` falla, y `createdAt` es `Instant`), `AppContainerTest` (`resetNavigation` vuelve a Catalog).
- **Integración (backend real, `reset` antes de cada test):** `AccountIntegrationTest` (`standard` → perfil con email y plan; `logout()` → sesión cerrada sin aviso, carrito vacío y `resetNavigation` llamado; token expirado → `didExpire` y carrito conservado y sin alerta; red caída → `genericError` y "Try again"); `OrdersIntegrationTest` (`buyer` → órdenes 2 y 1 con totales `45.00` y `109.99` y unidades 1 y 3; `standard` → lista vacía; `buyer` abre la 1 con subtotales `59.99` y `50.00`, total `109.99` y envío; `standard` abre la 1 y la 999 → `notFound`; red caída → `genericError`; una compra recién hecha aparece la primera).
- **Visual:** emulador y `@Preview`.

## Riesgos

- **Zona horaria del emulador:** los tests de formato usan zona fija; en el emulador se verifican fechas y no horas (la hora depende de la zona del dispositivo).
- **Cerrar sesión con pantallas apiladas:** se comprueba abriendo "My orders" y un detalle antes de cerrar sesión; el siguiente login debe abrir Catalog.
- **Token expirado en Account:** `ApiClient` termina la sesión y la raíz cambia; no debe aparecer la alerta genérica ni perderse el carrito.
- **Orden ajena:** el 404 es el mismo que el de una orden inexistente: un solo mensaje ("We couldn't find this order.").
