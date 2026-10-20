# Diseño: checkout

## Contexto

Tras `cart`: `CartRepository` (`items`, `totalQuantity`, `clear()`), `ApiClient.post(..., authorized = true)`, `ErrorMessages` (ya traduce `INSUFFICIENT_STOCK`, `PRODUCT_NOT_FOUND`, `EMPTY_ORDER` y `DUPLICATE_ORDER_ITEM`), `FieldErrorMapper` (genérico), `FormTextField`, `ErrorSummary`, `FieldErrorText`, `DropdownRow`, `Country`, `QuantityStepper` y el grafo `cart_graph` (`cart` → `checkout` provisional). El patrón de formulario es el del registro (A4).

Contrato (`POST orders`, requiere token): 201 con `id`, `status`, `paymentMethod`, `items[]` (`productId`, `sku`, `name`, `imageKey`, `quantity`, `unitPrice`, `subtotal`), `total`, `shipping` y `createdAt`; 409 `INSUFFICIENT_STOCK`; 404 `PRODUCT_NOT_FOUND`; 400 `EMPTY_ORDER`, `DUPLICATE_ORDER_ITEM` o `VALIDATION_ERROR` (campos `shipping.address`, `shipping.city`, `shipping.postalCode`, `shipping.country`, `paymentMethod`, `items…`). Si falla, el backend no descuenta stock. Diseño de iOS: `../ecommerce_shop_ios/openspec/changes/archive/2026-10-19-checkout/design.md`.

## Decisiones

### 1. Campos y reglas puros

- `CheckoutField` (`ADDRESS, CITY, POSTAL_CODE, COUNTRY, PAYMENT`) con `tagSuffix` (`address`, `city`, `postalcode`, `country`, `payment`) y `backendName` (`shipping.address`, `shipping.city`, `shipping.postalCode`, `shipping.country`, `paymentMethod`) y `fromBackend(name)`.
- `CheckoutForm` (`address`, `city`, `postalCode`, `country: Country?`, `payment: PaymentMethod = CARD`).
- `CheckoutValidator.validate(form): Map<CheckoutField, String>` (un `object` sin dependencias): recorta los textos; dirección 5–120, ciudad 2–60, código postal `^[A-Za-z0-9 -]{3,10}$`, país obligatorio. Orden: vacío → longitud/formato. Mensajes de la spec.
- `PaymentMethod` (`CARD`, `CASH`, `TRANSFER`) con `label` ("Card", "Cash", "Transfer"); el `name` es el valor del backend.

### 2. `Order` y DTOs

`Order(id, status, paymentMethod, items: List<OrderItem>, total, shipping, createdAt)`, `OrderItem(productId, sku, name, imageKey, quantity, unitPrice, subtotal)`, `Shipping(address, city, postalCode, country: String)` y `Order.totalUnits`. Los montos son `BigDecimal` (con `BigDecimalSerializer`). **`country` de `Shipping` es `String`** (el backend puede devolver `ES`). **`createdAt` queda como texto**: las fechas con hora (y su formato `Sep 15, 2026`) llegan en A8, que es la primera iteración que las muestra.

`CreateOrderRequestDto`: de cada línea del carrito solo `productId` y `quantity` (nunca nombre ni precio: el backend cobra el vigente), `paymentMethod` y `shipping` recortados.

### 3. `OrderRepository.createOrder(items, form): Order`

`POST orders` con `authorized = true`. No toca el carrito ni la sesión: lo decide el ViewModel. `orders()` y `order(id)` llegan en A8.

### 4. `CheckoutViewModel`

`CheckoutUiState`: `form`, `fieldErrors: Map<CheckoutField, List<String>>`, `hasSubmitted`, `isLoading`, `scrollTick`, `isConfirmingOrder`, `orderErrorMessage: String?`, `genericError`, `placedOrder: Order?`. `CheckoutViewModel(cart, orderRepository)`:

- Mismo patrón que `RegisterViewModel`: funciones `onAddressChange`… que borran el error del campo, `onFieldBlur` (revalida solo tras el primer intento) y `errorCount`/`firstErrorField`.
- `placeOrderTapped()`: valida; sin errores → `isConfirmingOrder = true` (abre "Place order?"); con errores, no llama al backend.
- `placeOrder(): Job`: ignora si `isLoading`; cierra `isConfirmingOrder`; envía las líneas actuales del carrito. **201 → `cart.clear()` y `placedOrder = order`**. Es el único punto donde se modifica el carrito.
- Errores: `INSUFFICIENT_STOCK`, `PRODUCT_NOT_FOUND`, `EMPTY_ORDER` y `DUPLICATE_ORDER_ITEM` → `orderErrorMessage = e.userMessage`; `VALIDATION_ERROR` → `FieldErrorMapper` con `CheckoutField.fromBackend` (campos en línea; si hay no mapeables, `orderErrorMessage = "We couldn't process your cart. Please try again."`); cualquier otro → `genericError`. Un 401 ya lo gestionó `ApiClient` (la raíz vuelve al login con el carrito intacto).
- **"Try again" de la alerta genérica llama a `placeOrderTapped()`**, no a `placeOrder()`: reabre "Place order?". Motivo: si la respuesta se perdió por la red, la orden pudo crearse; reenviar sin preguntar podría duplicarla.
- El ViewModel no navega: expone `placedOrder` y la pantalla reacciona.

### 5. Navegación y alcance

Dentro de `cart_graph`: `cart` → `checkout` → `confirmation` → `order/{id}` (provisional).

- `CheckoutViewModel` se crea con el `NavBackStackEntry` de `checkout` y la **confirmación lo reutiliza** (`getBackStackEntry(checkout)`) para leer `placedOrder`: así la confirmación muestra los datos de la respuesta 201 sin otra consulta y sin pasar la orden por la ruta.
- Con `placedOrder != null`, `CheckoutScreen` navega a `confirmation` (una vez: se consume).
- **Confirmación sin atrás:** `BackHandler(enabled = true) {}` y sin flecha. "Done": `popBackStack(Routes.CART, inclusive = false)` y `selectedTab = Catalog` (el catálogo se recarga al aparecer y muestra el stock nuevo). "View order": `navigate(order/{id})` (pantalla provisional con "Order #id" y atrás; A8 la sustituye).
- **"Review cart":** `popBackStack(Routes.CART, inclusive = false)`; el carrito se ajusta al stock al aparecer.

### 6. Pantallas

`CheckoutScreen`: misma estructura que el paso 2 del registro (`Column` con `verticalScroll` e `imePadding`, `BringIntoViewRequester` por campo, resumen, `FormTextField` para los tres textos con `onBlur`, `DropdownRow` en una tarjeta para el país, `SingleChoiceSegmentedButtonRow` para el pago, total estimado `checkout_total_text` y "Place order" `checkout_confirm_button`). El código postal usa `ImeAction.Done` y su acción cierra el teclado. Alertas (`AlertDialog` con `testTagsAsResourceId`): "Place order?" (`checkout_place_order_alert_*`), "Can't place your order" con "Review cart" (`checkout_order_error_alert_review_cart_button`) y la genérica con `onRetry = placeOrderTapped`. `LoadingOverlay` mientras envía.

`ConfirmationScreen(order, onViewOrder, onDone)`: número (`confirmation_order_id_text`, "Order #3"), una fila por línea (`confirmation_item_<id>`, sin fusionar a los hijos, con nombre, cantidad y subtotal), total del backend (`confirmation_total_text`), "View order" y "Done".

### 7. Pruebas

- **Unitarias sin red:** `CheckoutValidatorTest` (límites 4/5/120/121 de la dirección, 1/2/60/61 de la ciudad, código postal `SW1A 1AA` válido y `12`, `12#45`, `12345678901` no, solo espacios, país), `CheckoutFieldTest`, `OrderTest` (decodifica la respuesta 201 de la guía, redondeo a centavos, `totalUnits`), `CheckoutRequestDtoTest` (solo `productId`/`quantity`, textos recortados, pago como `CARD`).
- **Integración (`CheckoutIntegrationTest`, con `standard@shoplab.dev` y `reset` antes de cada test):** 2 del producto 1 y 1 del 4 → total `144.98` y carrito vacío; producto 3 → `orderErrorMessage` de stock insuficiente y carrito conservado; la última unidad del producto 5 comprada dos veces (la segunda rechazada); formulario vacío → 4 errores, `isConfirmingOrder` falso y ninguna llamada; `VALIDATION_ERROR` de campo (enviando directo una ciudad de 1 carácter) → error en `CITY`; backend caído → `genericError` y carrito intacto; "Try again" (`placeOrderTapped`) reabre la confirmación sin enviar; doble `placeOrder()` seguido crea una sola orden.
- **Visual:** emulador y `@Preview`.

## Riesgos

- **Orden duplicada tras perder el 201:** por eso "Try again" no reenvía y el usuario decide.
- **Gesto de atrás en la confirmación:** `BackHandler` lo bloquea; se prueba con la tecla y el gesto del emulador.
- **El menú del país abre un popup:** sus opciones se tocan por texto; el ID del desplegable está en la fila.
- **Teclado y botón:** `imePadding` + scroll, `Done` cierra el teclado (como en el registro).
- **El stock cambia entre el carrito y la compra:** el backend manda; el error sale con "Review cart".
- **Tamaño (L):** las tareas van por capas (reglas → datos → ViewModel → pantallas → navegación).
