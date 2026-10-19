# Diseño: cart

## Contexto

Tras `product-catalog`: `CartRepository` (`items: StateFlow`, `totalQuantity`, `isFull`, `quantityOf`, `maxAddable`, `add`, guardado como JSON), `ProductRepository.products()`, `ProductImageView`, `QuantityStepper`, `SnackbarCenter`, `GenericErrorDialog`, `Money.usd()` y el grafo de pestaña `cart_graph` con un `CartScreen` provisional. La insignia ya lee el carrito.

Referencias: Comportamiento §7; diseño de iOS `cart` (archivado).

## Decisiones

### 1. Operaciones nuevas de `CartRepository`

```kotlin
fun setQuantity(quantity: Int, productId: Long, stock: Int)  // entre 1 y min(stock, actual + 20 - total)
fun remove(productId: Long)
fun clear()
fun reconcile(products: List<Product>): Boolean              // true si algo cambió
```

- `setQuantity` recibe el `stock` porque el repositorio no lo guarda (un stock guardado se queda viejo): el máximo es `min(stock, cantidadActual + (20 − total))` y el resultado se acota a `1..máximo`. Guarda solo si cambia.
- `reconcile` baja cada cantidad a `min(cantidad, stock)` y quita las líneas de stock 0 o cuyo producto no aparece. Guarda y devuelve `true` solo si algo cambió.
- `clear` guarda el carrito vacío.
- Alternativa descartada: que `reconcile` también aplique el tope de 20 (las líneas ya lo cumplen; bajar cantidades solo reduce el total).

### 2. `CartViewModel`

`CartUiState(stock: Map<Long, Int>, genericError)`; las líneas salen de `cart.items` (un `StateFlow`, la pantalla las recoge). `CartViewModel(cart, productRepository, snackbar)`:

- `refresh(): Job`: `products()` → `cart.reconcile(products)` → si devuelve `true`, `snackbar.show(AlertText.Snackbar.CART_UPDATED)`; guarda el `stock` recibido. Si falla (`AppError`), `genericError` y el carrito no se toca (el ajuste solo se llama tras recibir los productos). "Try again" llama otra vez a `refresh()`.
- `maxQuantity(item) = min(stock[item.productId] ?: item.quantity, item.quantity + (20 − totalQuantity))`. **Sin stock conocido** (antes de la primera respuesta) el selector no deja subir.
- `setQuantity`, `remove` y `clear`, que delegan en el repositorio con el stock conocido.

El estado vacío y la existencia de "Checkout" salen de `cart.items.isEmpty()`.

### 3. Pantalla

`CartScreen(viewModel, cart, onCheckout)`: `LazyColumn` (`cart_list`) con una `CartLineRow` por línea (`cart_item_<id>`, **sin fusionar a los hijos**), y debajo un pie con `cart_total_text` ("Total" + monto), "Clear cart" (`cart_clear_button`) y "Checkout" (`cart_checkout_button`). Con el carrito vacío: `cart_empty_text` ("Your cart is empty") en lugar de la lista y de "Checkout". Se refresca con `LifecycleEventEffect(ON_RESUME)`.

`CartLineRow`: `ProductImageView`, nombre (`…_name_text`), `QuantityStepper` con la etiqueta `cart_item_<id>_quantity_stepper` (sus botones `-Decrement`/`-Increment` como en la hoja) y subtotal (`…_subtotal_text`, `unitPrice × quantity`).

**"Clear cart":** `AlertDialog` ("Remove all items from your cart?", "Clear cart" → `cart_clear_dialog_confirm_button`, "Cancel" → `cart_clear_dialog_cancel_button`) con `testTagsAsResourceId`. En Android no hay hoja de acciones; el catálogo de IDs ya lo prevé como diálogo.

### 4. Deslizar para quitar: componente propio

`SwipeToDismissBox` de Material no sirve para el requisito: o descarta al soltar, o vuelve; no deja un botón "Delete" a la espera de ser pulsado, que es lo que hace iOS y lo que usan las suites de Maestro. Se crea `SwipeToRevealRow` en `core/design`: un `Box` con el botón "Delete" detrás y el contenido encima, desplazable en horizontal con `Modifier.draggable` y un `Animatable`; al soltar queda abierto (revelando el botón, 96 dp) si pasó la mitad, y cerrado si no. El botón (`cart_item_<id>_delete_button`) **solo existe mientras la fila está abierta**, como en iOS. Sin APIs experimentales.

### 5. Checkout provisional y navegación

`Routes.CHECKOUT` dentro de `cart_graph`; "Checkout" navega a él y muestra un `PlaceholderScreen("Checkout")` con atrás, hasta A7. El carrito conserva su pila al cambiar de pestaña (grafo propio, A5).

### 6. Pruebas

- **Unitarias (`CartRepositoryTest`, ampliada):** `reconcile` baja cantidades, quita agotados e inexistentes y devuelve `false` si nada cambió; `setQuantity` no supera el stock ni el total de 20, no baja de 1 y guarda; `remove` y `clear`; el carrito vaciado se recupera vacío con un repositorio nuevo.
- **Integración (`CartIntegrationTest`, backend real):** un carrito con el producto 3 (stock 0) → tras `refresh()` la línea desaparece y se muestra el snackbar; con 1 unidad del producto 5 (stock 1) no cambia nada y no hay snackbar; 5 unidades de un producto con menos stock → baja; con el backend caído, `genericError` y carrito intacto; "Try again" repite el ajuste.
- **Visual:** emulador y `@Preview` (línea, vacío, abierta con "Delete").

## Riesgos

- **Datos viejos hasta que termina la petición:** el selector no sube sin stock conocido y el ajuste ocurre en cada aparición.
- **Gesto de deslizar:** `draggable` dentro de un `LazyColumn` vertical convive bien (orientaciones distintas); Maestro lo activa con `swipe`. Se prueba en el emulador con un gesto real.
- **IDs:** `cart_item_<id>` sin `mergeDescendants` (`clickableKeepingChildren` si hiciera falta); "Delete" solo existe tras deslizar.
- **Un 401 de token no se muestra como error:** `ApiClient` ya cerró la sesión y la raíz vuelve al login.
