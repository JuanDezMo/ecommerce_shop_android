# Tareas: cart

Verificación (desde la raíz del repositorio):

- **UNIT:** `./gradlew :app:testDebugUnitTest --tests '<Clase>'` (sin red).
- **ALL:** `./gradlew :app:testDebugUnitTest`. **Requiere el backend levantado** (`docker compose up` en `../ecommerce_shop_api`, perfil `testing`; `curl http://localhost:8080/actuator/health` responde `UP`).
- **BUILD:** `./gradlew :app:assembleDebug :app:lintDebug`.
- **IDs:** `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml`.

IDs a revisar en la tarea 4.3: `cart_list`, `cart_item_<id>` y sus `_name_text`, `_quantity_stepper` (con `-Increment`/`-Decrement`), `_subtotal_text` y `_delete_button` (solo tras deslizar), `cart_total_text`, `cart_clear_button`, `cart_clear_dialog_confirm_button`, `cart_clear_dialog_cancel_button`, `cart_checkout_button`, `cart_empty_text` y `app_snackbar_text`.

Antes de empezar: `product-catalog` está commiteado y archivado.

## 1. Repositorio (test primero)

- [x] 1.1 Ampliar `CartRepositoryTest` (falla): `reconcile` baja una cantidad al stock (5 → 1 con stock 1), quita las líneas de stock 0 y de productos que ya no existen, devuelve `true` si cambió algo y `false` si no, y no guarda si no cambió; `setQuantity` sube hasta el stock (stock 3 con 3: no pasa de 3), no pasa del tope de 20 (con 18 en total y la línea en 2: llega a 4), no baja de 1, y guarda; `remove` quita solo esa línea; `clear` deja el carrito vacío y un repositorio nuevo sobre el mismo almacén lo recupera vacío. Verificar: UNIT falla por no compilar.
- [x] 1.2 Implementar `setQuantity`, `remove`, `clear` y `reconcile` en `CartRepository` (design.md §1). Verificar: `CartRepositoryTest` pasa con UNIT.

## 2. ViewModel (test primero)

- [x] 2.1 Escribir `CartIntegrationTest` (falla), con `CartViewModel`, `CartRepository` sobre `InMemoryStore` y backend real (**requiere backend**): un carrito con el producto 3 → tras `refresh()` la línea desaparece y `snackbar.message` es "Your cart was updated to match available stock."; con 1 unidad del producto 5 no cambia nada y no hay snackbar; 5 unidades del producto 5 (stock 1) bajan a 1; `maxQuantity` sin haber llamado a `refresh()` es la cantidad actual (no deja subir) y tras `refresh()` es `min(stock, actual + 20 − total)`; con base URL `http://localhost:9/api/v1/` → `genericError` de red y el carrito intacto; "Try again" (otro `refresh()` con backend disponible) ajusta y limpia el error; `setQuantity`, `remove` y `clear` desde el ViewModel cambian el carrito. Crear `CartUiState.kt`, `CartViewModel.kt` (design.md §2) y su fábrica. Verificar: ALL falla por no compilar y después pasa.
- [x] 2.2 Añadir `Cart.*` a `TestTags` (los IDs de la cabecera; con funciones para los de línea) y `AlertText.ClearCart` ("Remove all items from your cart?", "Clear cart", "Cancel"). Verificar: `TestTagsTest` pasa.

## 3. Pantalla

- [x] 3.1 Crear `core/design/SwipeToRevealRow.kt` (design.md §4) con `@Preview` de la fila cerrada y abierta. Verificar: BUILD pasa.
- [x] 3.2 Crear `features/cart/CartLineRow.kt` y `CartScreen.kt` (design.md §3): lista, pie con total, "Clear cart" (con su diálogo) y "Checkout", estado vacío y refresco en `ON_RESUME`, con la alerta genérica ("Try again" repite el ajuste). Incluir `@Preview` con líneas, vacío y el diálogo en claro y oscuro. Verificar: BUILD pasa.
- [x] 3.3 Añadir `Routes.CHECKOUT` y un `CheckoutScreen` provisional con atrás (design.md §5) y conectar la pestaña Cart en `ShopApp`. Verificar: BUILD pasa y la pestaña Cart muestra el estado vacío con el carrito vacío.

## 4. Validación en el emulador (backend levantado, `reset` antes)

- [x] 4.1 Ejecutar ALL, BUILD y `assembleRelease`. Verificar: todo pasa, incluidos los de las iteraciones anteriores.
- [x] 4.2 Entrando con `standard@shoplab.dev`, en claro y oscuro:
  - añadir 2 del producto 1 y 1 del producto 4 desde el catálogo; la pestaña Cart muestra las dos líneas con subtotales `$119.98` y `$25.00` y el total `$144.98`, en el orden en que se añadieron;
  - el selector de cada línea: sube hasta su máximo (por stock o por el tope de 20) y no baja de 1; el subtotal, el total y la insignia se actualizan;
  - deslizar una línea a medio camino y soltar: queda abierta si pasó la mitad y cerrada si no; "Delete" quita solo esa línea y actualiza total e insignia;
  - "Clear cart" → confirmar vacía y muestra "Your cart is empty" sin "Checkout"; "Cancel" no cambia nada;
  - "Checkout" abre el checkout provisional y atrás vuelve al carrito;
  - ajuste al stock: dejar el producto 3 (stock 0) en el carrito guardado (por ejemplo, con `reset` y un carrito sembrado) o bajar el stock desde el backend, abrir la pestaña Cart: la línea se ajusta y sale el snackbar; sin cambios no sale;
  - con el backend apagado: alerta genérica, el carrito no cambia y "Try again" funciona al volver;
  - cerrar y abrir la app conserva las cantidades editadas.

  Verificar: cada caso se comporta como pide `specs/cart-screen/spec.md`; anotar lo que difiera y corregirlo.
- [x] 4.3 Revisar los IDs con `uiautomator dump` (los de la cabecera), en especial `cart_item_<id>_delete_button` (solo existe con la fila abierta), el selector de cada línea y los botones del diálogo de vaciar. Si alguno no llega, anotarlo en el catálogo (las tres copias) y en el plan A9. Verificar: los IDs se encuentran o hay nota.
- [x] 4.4 Actualizar `docs/plan-iteraciones-android.md` (criterios de la A6 y la decisión del componente de deslizar) y `docs/shop-android-arquitectura.md` si algo cambió. Verificar: casillas marcadas.

## Seguimiento

- El responsable revisa y hace el commit; después se archiva con `/opsx:archive`.

## Resultados de la verificación

- 190 tests JVM pasan (incluidos los de integración contra el backend real); `assembleDebug`, `assembleRelease` y `lintDebug` sin errores.
- Emulador (Pixel 6a, Android 17), claro y oscuro: dos líneas con subtotales `$119.98` y `$75.00` y total `$194.98`, en el orden en que se añadieron; el selector sube hasta el stock (10) y deshabilita el "+" ahí, y no baja de 1; un deslizamiento corto deja la fila cerrada (sin "Delete") y uno largo la deja abierta con "Delete" (`cart_item_<id>_delete_button` solo existe abierta); "Delete" quita solo esa línea y actualiza total e insignia; "Clear cart" → "Cancel" conserva todo y → "Clear cart" muestra "Your cart is empty" sin "Checkout"; "Checkout" abre el checkout provisional y atrás vuelve; el ajuste al stock (carrito sembrado con el producto 3 y 5 unidades del 5): la línea del 3 desaparece, el 5 baja a 1 y sale el snackbar, y una segunda visita sin cambios no lo muestra; con el backend apagado: alerta genérica y el carrito intacto.
- IDs (`uiautomator dump`): llegan `cart_list`, `cart_item_<id>` y los de sus hijos (nombre, selector con `-Increment`/`-Decrement`, subtotal), `cart_total_text`, `cart_clear_button`, `cart_checkout_button`, `cart_empty_text`, `cart_item_<id>_delete_button` (solo tras deslizar) y los botones del diálogo de vaciar.
- Hallazgo: el carrito sembrado para probar el ajuste se escribió en `shared_prefs/shop.xml` con `adb shell run-as` (la app de debug es `debuggable`), sin tocar el backend.
