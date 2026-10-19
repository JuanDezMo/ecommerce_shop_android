# Propuesta: cart

## Why

El usuario ya puede añadir productos, pero no verlos: la pestaña Cart sigue siendo un texto provisional. Hace falta la pantalla del carrito para revisar lo que se va a comprar, corregir cantidades, quitar productos, vaciarlo y llegar al checkout. Además el stock cambia mientras el carrito está guardado en el dispositivo (otro usuario compra, un producto se agota o desaparece), así que el carrito debe ajustarse al stock real cada vez que se abre, antes de que el usuario intente comprar. Es la iteración A6 de `docs/plan-iteraciones-android.md`.

## What Changes

- **Lista del carrito:** por línea, imagen, nombre, selector de cantidad y subtotal estimado; debajo, el total estimado. Una línea por producto, en el orden en que se añadieron.
- **Editar cantidades** con el selector de cada línea (de 1 al menor entre el stock y lo que falte para llegar a 20 en total).
- **Quitar una línea** deslizándola ("Delete") y **vaciar el carrito** con "Clear cart", que pide confirmar.
- **Estado vacío** (`cart_empty_text`); "Checkout" solo existe si hay líneas y abre un checkout provisional (el real llega en A7).
- **Ajuste al stock real** cada vez que la pantalla aparece: se piden los productos, se baja cada cantidad al stock y se quitan los agotados o inexistentes; si algo cambió, snackbar "Your cart was updated to match available stock."; si falla, alerta genérica con "Try again" y el carrito no se toca.
- **`CartRepository`** gana las operaciones que le faltaban: `setQuantity`, `remove`, `clear` y `reconcile`.
- **IDs `Cart.*`** en `TestTags` y los textos del diálogo de vaciar en `AlertText`.

## Capabilities

### New Capabilities

- `cart-screen`: qué ve y qué puede hacer el usuario en el carrito: lista y totales, edición de cantidades, quitar y vaciar, estado vacío, acceso al checkout y ajuste al stock real con su aviso y su error.

### Modified Capabilities

Ninguna: las reglas de límites del carrito ya las fija `add-to-cart`.

## Impact

- **Código nuevo:** `features/cart/{CartUiState,CartViewModel,CartScreen,CartLineRow}.kt`, `features/checkout/CheckoutScreen.kt` (provisional), `core/design/SwipeToRevealRow.kt`.
- **Código modificado:** `data/repository/CartRepository.kt`, `app/Routes.kt` (`checkout`), `app/ShopApp.kt` (pestaña Cart), `app/ViewModelFactories.kt`, `core/errors/AlertText.kt`, `core/testing/TestTags.kt`.
- **Tests:** ampliar `CartRepositoryTest` e integración `CartIntegrationTest` contra el backend real (**necesita `docker compose up`**).
- **Dependencias:** ninguna nueva.
- **Fuera de alcance:** el checkout real, vaciar el carrito al comprar y al cerrar sesión (A7 y A8) y "Review cart" (A7, reutiliza el ajuste de esta).
