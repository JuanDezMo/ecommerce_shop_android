# Diseño: product-catalog

## Contexto

Ya existen `Product`, `ProductDto`, `ProductRepository` (`products()`, `product(id)`), `Money`, `BigDecimalSerializer`, `KeyValueStore`/`InMemoryStore`, `SnackbarCenter`, `GenericErrorDialog`, `LoadingOverlay` y la navegación por pestañas con `selectedTab`. Faltan imágenes, carrito y pantallas.

Referencias: Comportamiento §6, §7.1 y §14; Arquitectura §6 y §9; diseños de iOS `product-images`, `product-catalog` (archivados).

## Decisiones

### 1. Imágenes: SVG → vector drawable

La Arquitectura §9.1 prevé *Vector Asset* de Android Studio; como no se puede automatizar, se usa un **conversor propio de una sola vez** (`scripts/svg_to_vector.py`, solo para estos 7 SVG, que solo usan `rect` con `rx`, `circle`, `path` y trazos): cada elemento se traduce a un `<path>` con `fillColor`/`strokeColor`, y `rect rx` y `circle` se expresan con arcos. El SVG sigue siendo la fuente; el script se conserva para regenerar. Los 7 vectores se revisan a la vista (claro y oscuro) contra los PNG de `docs/imagenes-producto/png/`.

`ProductImage.resourceFor(imageKey)` es el único mapeo (claves conocidas → recurso; cualquier otra, incluida `""`, → `product_placeholder`; distingue mayúsculas). `ProductImageView(imageKey, isSoldOut, soldOutTag)`: `Box` cuadrado con `Image` (`testTag("product_image_<imageKey>")` con la clave **recibida**, también con el respaldo) y, si está agotado, la etiqueta "Sold out" centrada con el ID que pase la pantalla.

### 2. `CartItem` y `CartRepository`

`CartItem(productId, name, imageKey, unitPrice: BigDecimal, quantity)`, `@Serializable` (precio con `BigDecimalSerializer`). `CartRepository(store: KeyValueStore)`:

- `items: StateFlow<List<CartItem>>`, `totalQuantity`, `isFull` (≥ 20), `quantityOf(productId)`.
- `maxAddable(product) = max(0, min(stock - quantityOf, 20 - total))`.
- `add(product, quantity)`: recorta a `maxAddable`; suma a la línea existente o crea una (una línea por producto).
- Guardado como JSON en la clave `cart.items` de `KeyValueStore` (SharedPreferences normal: el carrito no es sensible). Se lee al crear el repositorio; un JSON ilegible se ignora (carrito vacío) en lugar de cerrar la app.
- `setQuantity`, `remove`, `clear` y `reconcile` (A6) no se implementan todavía: se añaden con la pantalla que los usa.

`AppContainer.cart` (usa el mismo `store` que `settings`).

### 3. `AddToCartAction` y `AddToCartFlow`

`addToCartAction(product, cart)` es una función pura: `Disabled` si el producto está agotado o el carrito ya tiene todo su stock; `CartFull` si no y el carrito tiene 20; `OpenSheet` en otro caso. La comparten fila y detalle.

`AddToCartFlow` (clase simple por pantalla, con `StateFlow`/estado de Compose): `sheetProduct: Product?` y `showCartFull: Boolean`; `start(product)` aplica la acción. Un `@Composable AddToCartHost(flow, cart, snackbar)` dibuja la hoja y la alerta; así catálogo y detalle no repiten la lógica.

### 4. `AddToCartSheet`

`ModalBottomSheet` con `testTag("add_to_cart_sheet")`, nombre (`add_to_cart_name_text`), selector de cantidad y "Add to cart" (`add_to_cart_confirm_button`). **El selector es propio** (Android no tiene `Stepper`): `QuantityStepper(value, range, tag)` con dos `IconButton` (`<tag>-Decrement`, `<tag>-Increment`) y el valor en medio (`<tag>`). Los nombres de los botones imitan los que genera iOS para reutilizar los flujos de Maestro; el máximo se alcanza y el "+" se deshabilita. Confirmar → `cart.add`, cerrar y `snackbar.show("Added to cart")`. Cerrar la hoja sin confirmar no añade nada. La cantidad máxima se calcula al abrir la hoja (`maxAddable`).

### 4 bis. Alerta "Cart is full"

`AlertDialog` ("Cart is full" / "Your cart can hold up to 20 items." / "OK" con ID `app_cart_full_alert_ok_button`), con `semantics { testTagsAsResourceId = true }` en el `modifier` (A2).

### 5. Catálogo

`CatalogUiState(products, isLoading, loadError: Boolean, genericError)`. `CatalogViewModel(productRepository)`:

- `load()` (devuelve `Job` para los tests): `isLoading` → `products` o error. En error: `genericError = e` y `loadError = false`; `dismissGenericError()` (Cancel) deja `loadError = true` (el texto de `catalog_error_text`); "Try again" vuelve a llamar a `load()`. Una carga correcta limpia ambos.
- Se llama al aparecer la pantalla (`LifecycleEventEffect(ON_RESUME)`, que también cubre volver del detalle y de otra pestaña) y desde `PullToRefreshBox`.
- Con datos ya cargados, una recarga no vuelve a mostrar el indicador a pantalla completa (lo muestra el gesto de refresco); el indicador a pantalla completa solo se usa en la primera carga.

`CatalogScreen`: `PullToRefreshBox` con `LazyColumn` (`catalog_list`); cada fila `catalog_item_<id>` es un contenedor **sin `mergeDescendants`** (si no, desaparecen los IDs de sus hijos): imagen, `…_name_text`, `…_price_text`, `…_soldout_text` y botón `…_add_button`. Tocar la fila abre el detalle; tocar el botón **no** (el botón consume el toque). Precio con `Money.format`.

### 6. Detalle

`ProductDetailViewModel(productRepository, productId)` (`load()`, `genericError`, `dismiss…`). `PRODUCT_NOT_FOUND` va a la alerta genérica con su mensaje (ya está en `ErrorMessages`). "Try again" repite `load()`; "Cancel" deja la pantalla sin datos (con atrás para volver). `ProductDetailScreen`: imagen, nombre, descripción, precio, `product_detail_stock_text` ("10 in stock" / "1 in stock") o `product_detail_soldout_text`, y `product_detail_add_button` con la misma regla. El producto de la lista no se reutiliza: se pide `GET /products/{id}`.

### 7. Navegación por pestaña e insignia

Cada pestaña es un **grafo** con su propia pila: `catalog_graph` (`catalog`, `product/{id}`), `cart_graph` (`cart`) y `account_graph` (`account`). `Tab.route` pasa a ser la ruta del grafo y la navegación entre pestañas usa `popUpTo(startDestination) { saveState = true }`, `launchSingleTop` y `restoreState`, de modo que cada pestaña conserva su pila al cambiar (patrón estándar de Navigation Compose). `Routes.kt` concentra las rutas y `productRoute(id)`.

La insignia: en `NavigationBarItem`, el icono del carrito va en un `BadgedBox` con el total (`cart.totalQuantity`), solo si es mayor que 0.

### 8. Pruebas

- **Unitarias sin red:** `ProductImageTest` (seis claves con su recurso `R.drawable.product_*` distinto del respaldo, `unknown` y `""` → respaldo, distingue mayúsculas); `CartRepositoryTest` con `InMemoryStore` (la misma lista de iOS: línea única, tope de stock, tope de 20, `maxAddable`, `isFull`, persistencia con el precio intacto, JSON ilegible); `AddToCartActionTest` (tres acciones y casos límite); `CatalogViewModelTest` donde no necesite red.
- **Integración (backend real):** `CatalogIntegrationTest` (seis productos en orden, precio `59.99` y `25.00`, producto 3 agotado, `product(99)` → `PRODUCT_NOT_FOUND`, error de carga con base URL sin servidor, "Cancel" → `loadError`, "Try again" con backend disponible) y `ProductDetailIntegrationTest`.
- **Visual:** emulador y `@Preview` (siete imágenes con y sin "Sold out", filas, hoja y estados de error).

## Riesgos

- **Conversión SVG → vector:** los `rx` y los trazos pueden verse distinto; se compara con los PNG y se corrige el script.
- **`PullToRefreshBox`** y `ModalBottomSheet` son APIs experimentales de Material 3 (`@OptIn`); versión fijada por el BOM.
- **Fila clicable con botón dentro:** el toque del botón no debe abrir el detalle; se comprueba en el emulador.
- **IDs:** `catalog_item_<id>` sin `mergeDescendants`, y `product_image_<clave>` en la imagen, no en el contenedor; los IDs del selector y de la hoja se revisan con `uiautomator dump` (la hoja es una ventana aparte: repetir `testTagsAsResourceId`).
- **Tamaño (L):** las tareas van por capas (imágenes → carrito → catálogo → detalle y hoja → navegación) y cada una se verifica antes de seguir.
