# Tareas: checkout

Verificación (desde la raíz del repositorio):

- **UNIT:** `./gradlew :app:testDebugUnitTest --tests '<Clase>'` (sin red).
- **ALL:** `./gradlew :app:testDebugUnitTest`. **Requiere el backend levantado** (`docker compose up` en `../ecommerce_shop_api`, perfil `testing`; `curl http://localhost:8080/actuator/health` responde `UP`). Estos tests crean órdenes: hacen `reset` antes de cada uno.
- **BUILD:** `./gradlew :app:assembleDebug :app:lintDebug`.
- **IDs:** `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml`.

IDs a revisar en la tarea 5.3: los `checkout_*` (campos, `_error_text`, total, botón, resumen, botones de las dos alertas) y los `confirmation_*` (`order_id_text`, `total_text`, `item_<id>`, `view_order_button`, `done_button`).

Antes de empezar: `cart` está commiteado y archivado.

## 1. Modelos y reglas (sin red)

- [x] 1.1 Escribir `CheckoutFieldTest` (falla): orden `ADDRESS, CITY, POSTAL_CODE, COUNTRY, PAYMENT`; `tagSuffix` (`address`, `city`, `postalcode`, `country`, `payment`) y `backendName` (`shipping.address`, `shipping.city`, `shipping.postalCode`, `shipping.country`, `paymentMethod`); `fromBackend("shipping.postalCode") == POSTAL_CODE` y un nombre desconocido (por ejemplo `items[0].quantity`) da `null`. Crear `data/model/PaymentMethod.kt` (`CARD`, `CASH`, `TRANSFER` con `label`), `features/checkout/CheckoutField.kt` y `CheckoutForm.kt`. Verificar: UNIT falla por no compilar y después pasa.
- [x] 1.2 Escribir `CheckoutValidatorTest` (falla): dirección de 4, 5, 120 y 121 caracteres; vacía y solo espacios → "Enter your address."; ciudad de 1, 2, 60 y 61 ("Enter your city." / "City must be 2 to 60 characters."); código postal `SW1A 1AA` válido y `12`, `12#45`, `12345678901` con "Use 3 to 10 letters, numbers, spaces or hyphens.", vacío → "Enter your postal code."; país sin seleccionar → "Select your country."; los textos se recortan antes de validar; un formulario válido no da errores. Crear `CheckoutValidator.kt` (design.md §1). Verificar: UNIT falla y después pasa.
- [x] 1.3 Escribir `FieldErrorMapperTest` ampliado con `CheckoutField.fromBackend` (cada campo del backend va a su campo; `items[0]` y `shipping` no se reconocen → `hasUnmappedErrors`). Verificar: UNIT pasa (el mapper ya es genérico).

## 2. Datos

- [x] 2.1 Escribir `OrderTest` (falla): decodifica la respuesta 201 de la guía de integración (`id` 3, `CONFIRMED`, dos líneas, `total` `144.98`, `shipping`, `createdAt` como texto) y `totalUnits == 3`; los montos conservan sus dos decimales. Escribir `CheckoutRequestDtoTest` (falla): el JSON lleva de cada línea solo `productId` y `quantity` (ni nombre ni precio), `paymentMethod` `CARD`, `shipping` con los textos recortados y el país como código. Crear `data/model/Order.kt`, `data/remote/dto/OrderDto.kt` y `CreateOrderRequestDto.kt`. Verificar: UNIT falla por no compilar y después pasa.
- [x] 2.2 Añadir `AlertText.PlaceOrder` ("Place order?", "Estimated total: $X.", "Place order", "Cancel"), `AlertText.OrderError` ("Can't place your order", "Review cart", "We couldn't process your cart. Please try again.") y `TestTags.Checkout.*`/`Confirmation.*`. Verificar: `TestTagsTest` pasa.
- [x] 2.3 Crear `data/repository/OrderRepository.kt` (`createOrder(items, form)`, `POST orders` con token) y `AppContainer.orderRepository`. Se prueba en 3.x con el backend. Verificar: BUILD pasa.

## 3. ViewModel (test primero)

- [x] 3.1 Escribir `CheckoutIntegrationTest` (falla), con `CheckoutViewModel`, un `CartRepository` sobre `InMemoryStore` y backend real (**requiere backend**; iniciar sesión con `standard@shoplab.dev` antes de cada test): 2 unidades del producto 1 y 1 del 4 → `placedOrder` con total `144.98`, carrito vacío; carrito con el producto 3 (agotado) → `orderErrorMessage` de stock insuficiente y carrito intacto; comprar la última unidad del producto 5 y luego otra vez → la segunda con `orderErrorMessage`; formulario vacío → 4 errores, `isConfirmingOrder` falso y ninguna llamada; ciudad de 1 carácter enviada directo → error en `CITY` y carrito intacto; backend caído → `genericError` y carrito intacto; "Try again" (`placeOrderTapped()`) con la alerta genérica reabre `isConfirmingOrder` sin enviar nada; dos `placeOrder()` seguidos crean una sola orden. Crear `CheckoutUiState.kt`, `CheckoutViewModel.kt` (design.md §4) y su fábrica. Verificar: ALL falla por no compilar y después pasa.

## 4. Pantallas

- [x] 4.1 Reescribir `features/checkout/CheckoutScreen.kt` (design.md §6): formulario con resumen, `FormTextField`, país con `DropdownRow`, pago segmentado, total y "Place order"; alertas "Place order?", "Can't place your order" y genérica (`onRetry = placeOrderTapped`); `LoadingOverlay`. Incluir `@Preview` en claro, oscuro y con errores. Verificar: BUILD pasa.
- [x] 4.2 Crear `features/confirmation/ConfirmationScreen.kt` (número, líneas, total, "View order" y "Done"; sin flecha) y `features/orders/OrderDetailScreen.kt` provisional ("Order #id" con atrás). Incluir `@Preview` de la confirmación en claro y oscuro. Verificar: BUILD pasa.
- [x] 4.3 Conectar la navegación (design.md §5): `Routes.CONFIRMATION` y `Routes.ORDER`, el `CheckoutViewModel` compartido con la confirmación, `BackHandler` en la confirmación, "Done" (vuelve al carrito y selecciona Catalog) y "Review cart". Verificar: BUILD pasa y el flujo carrito → checkout abre el formulario.

## 5. Validación en el emulador (backend levantado, `reset` antes)

- [x] 5.1 Ejecutar ALL, BUILD y `assembleRelease`. Verificar: todo pasa, incluidos los de las iteraciones anteriores.
- [x] 5.2 Entrando con `standard@shoplab.dev`, con el carrito de 2 unidades del producto 1 y 1 del 4, en claro y oscuro y con el teclado abierto:
  - el checkout muestra los valores iniciales y el total estimado `$144.98`;
  - "Place order" vacío: "4 fields need attention", error bajo cada campo, desplazamiento al primero y ninguna alerta;
  - las reglas (dirección corta, código `12#45`, país sin elegir) con sus mensajes; corregir quita el error y salir de un campo vacío tras el primer intento lo marca;
  - datos válidos → "Place order?" con "Estimated total: $144.98."; "Cancel" vuelve al formulario con lo escrito sin enviar;
  - "Place order" → confirmación con el número, las dos líneas y el total `$144.98`; el carrito y la insignia quedan vacíos; sin flecha, y el gesto y el botón de atrás del sistema no salen de la confirmación;
  - "View order" abre el detalle provisional y atrás vuelve a la confirmación; "Done" lleva al catálogo con el stock actualizado (el producto 1 con 8 unidades en su detalle);
  - el producto 3 sembrado en el carrito → "Can't place your order" con el mensaje de stock; "Review cart" vuelve al carrito, que se ajusta al stock; el carrito no cambia antes;
  - la última unidad del producto 5 comprada dos veces → la segunda muestra "Can't place your order";
  - backend apagado → alerta genérica y el carrito intacto; "Try again" reabre "Place order?" sin enviar; al volver el backend, confirmar compra bien.

  Verificar: cada caso se comporta como pide `specs/checkout/spec.md` y `specs/order-confirmation/spec.md`; anotar lo que difiera y corregirlo.
- [x] 5.3 Revisar los IDs con `uiautomator dump` (los de la cabecera), en especial el desplegable de país, el segmentado de pago, `checkout_<campo>_error_text` solo con error y los botones de las dos alertas. Si alguno no llega, anotarlo en el catálogo (las tres copias) y en el plan A9. Verificar: los IDs se encuentran o hay nota.
- [x] 5.4 Actualizar `docs/plan-iteraciones-android.md` (criterios de la A7) y `docs/shop-android-arquitectura.md` si alguna decisión cambió (por ejemplo, el alcance del `CheckoutViewModel` compartido). Verificar: casillas marcadas.

## Seguimiento

- El responsable revisa y hace el commit; después se archiva con `/opsx:archive`.

## Resultados de la verificación

- 221 tests JVM pasan (incluidos los de integración contra el backend real, que crean órdenes); `assembleDebug`, `assembleRelease` y `lintDebug` sin errores.
- Emulador (Pixel 6a, Android 17), claro y oscuro: el checkout abre con los campos vacíos, Card elegida y el total estimado `$144.98`; "Place order" vacío → "4 fields need attention" con un error bajo cada campo y ninguna alerta; con dirección corta y código `12#45` → "3 fields need attention" con sus mensajes; datos válidos → "Place order?" con "Estimated total: $144.98."; "Cancel" vuelve al formulario sin enviar; "Place order" → confirmación con "Order #3", las dos líneas con sus subtotales y el total `$144.98`, el carrito y la insignia vacíos; sin flecha y el botón de atrás del sistema no sale de la confirmación; "View order" abre el detalle provisional y atrás vuelve a la confirmación; "Done" lleva al catálogo (el producto 1 muestra "8 in stock") y el carrito queda vacío.
- Errores: con el backend apagado, confirmar → alerta genérica y, al pulsar "Try again", se vuelve a abrir "Place order?" con el total (sin enviar nada); al volver el backend, confirmar con el producto 3 (agotado) sembrado en el carrito → "Can't place your order" con el mensaje de stock, y "Review cart" vuelve al carrito, que se ajusta (quita el producto 3 y muestra el snackbar). Los casos de la última unidad del producto 5 comprada dos veces, `VALIDATION_ERROR` con campos y doble envío están cubiertos por `CheckoutIntegrationTest`.
- IDs (`uiautomator dump`): llegan los `checkout_*` (campos, `_error_text` solo con error, total, botón, resumen, desplegable de país, segmentado de pago y los botones de las dos alertas) y los `confirmation_*` (número, total, `confirmation_item_<id>` con sus hijos, "View order" y "Done").
- Hallazgos: el ajuste del carrito al abrir Cart quita los productos agotados antes de llegar al checkout, así que para probar "Can't place your order" hay que apagar el backend al abrir Cart (el ajuste falla y el carrito sigue como estaba) y encenderlo antes de confirmar. El `CheckoutViewModel` con el alcance de la entrada `checkout` y reutilizado por la confirmación funcionó sin pasar la orden por la ruta.
