# Propuesta: product-catalog

## Why

La app ya tiene sesión y registro, pero la pestaña Catalog es un texto provisional: no se ven productos, no hay imágenes ni forma de empezar una compra. El catálogo es la primera pantalla con datos del backend tras el login y la base de las siguientes (carrito, checkout, órdenes), que reutilizan la imagen de producto y las reglas del carrito. Es la iteración A5 de `docs/plan-iteraciones-android.md`: fusiona las iteraciones iOS `product-images`, `product-catalog` y `add-to-cart`, porque las imágenes ya están decididas (los 7 SVG de iOS) y las reglas del carrito están validadas.

## What Changes

- **Imágenes de producto:** los 7 SVG de `docs/imagenes-producto/` convertidos a *vector drawable* (`product_<clave>` y `product_placeholder`), con un único mapeo `imageKey` → recurso y un componente `ProductImageView` (ID `product_image_<imageKey>`, etiqueta "Sold out" superpuesta).
- **Catálogo:** lista con imagen, nombre, precio y botón "Add to cart" por fila; se recarga al aparecer la pantalla y con deslizar para recargar; "Sold out" y botón deshabilitado con `stock` 0; error de carga con alerta genérica y, tras "Cancel", el texto `catalog_error_text`.
- **Detalle de producto:** imagen, nombre, descripción, precio y stock ("10 in stock" / "Sold out"), dentro de la pila de la pestaña Catalog (la barra sigue visible; atrás vuelve a la lista); `PRODUCT_NOT_FOUND` con su mensaje.
- **Carrito (reglas y guardado):** `CartRepository` con una línea por producto, máximo 20 unidades, nunca más que el stock, guardado en el dispositivo (JSON). La pantalla del carrito llega en A6.
- **Añadir al carrito:** regla única del botón (`Disabled`, `CartFull`, `OpenSheet`) compartida por fila y detalle; hoja `ModalBottomSheet` con selector de cantidad, snackbar "Added to cart" y alerta "Cart is full".
- **Insignia** del carrito en la pestaña Cart (total de unidades).
- **Navegación:** cada pestaña pasa a ser un grafo con su propia pila (`catalog_graph`, `cart_graph`, `account_graph`).
- **IDs:** `Catalog.*`, `ProductDetail.*`, `AddToCart.*` y el de la alerta "Cart is full" en `TestTags`; los botones del selector de cantidad se llaman `add_to_cart_quantity_stepper-Increment` y `-Decrement`, igual que en iOS, para reutilizar los flujos de Maestro.

## Capabilities

### New Capabilities

- `product-catalog`: lista, detalle, agotados y errores de carga.
- `product-image-display`: qué imagen se muestra por producto, su ID y la etiqueta "Sold out".
- `add-to-cart`: límites del carrito, botón "Add to cart", hoja, insignia y persistencia.

### Modified Capabilities

Ninguna.

## Impact

- **Recursos nuevos:** `res/drawable/product_*.xml` (7).
- **Código nuevo:** `core/design/{ProductImage,ProductImageView}.kt`, `data/model/CartItem.kt`, `data/repository/CartRepository.kt`, `features/productdetail/{AddToCartAction,AddToCartFlow,AddToCartSheet,ProductDetailUiState,ProductDetailViewModel,ProductDetailScreen}.kt`, `features/catalog/{CatalogUiState,CatalogViewModel,CatalogScreen}.kt`, `app/Routes.kt`.
- **Código modificado:** `app/ShopApp.kt` (grafos por pestaña, insignia), `app/Tabs.kt`, `app/AppContainer.kt` (`cart`), `app/ViewModelFactories.kt`, `core/errors/AlertText.kt`, `core/testing/TestTags.kt`.
- **Documentos:** `docs/catalogo-ids-accesibilidad.md` (nota de los botones del selector; las tres copias).
- **Tests:** unitarios (`ProductImageTest`, `CartRepositoryTest`, `AddToCartActionTest`, `CatalogViewModelTest`) e integración (`CatalogIntegrationTest`, backend real).
- **Dependencias:** ninguna nueva.
- **Fuera de alcance:** la pantalla del carrito, su refresco de stock y borrado (A6), el logout que vacía el carrito (A8).
