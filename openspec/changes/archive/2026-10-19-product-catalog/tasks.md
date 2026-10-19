# Tareas: product-catalog

Verificación (desde la raíz del repositorio):

- **UNIT:** `./gradlew :app:testDebugUnitTest --tests '<Clase>'` (sin red).
- **ALL:** `./gradlew :app:testDebugUnitTest`. **Requiere el backend levantado** (`docker compose up` en `../ecommerce_shop_api`, perfil `testing`; `curl http://localhost:8080/actuator/health` responde `UP`).
- **BUILD:** `./gradlew :app:assembleDebug :app:lintDebug`.
- **IDs:** `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml`.

IDs a revisar en la tarea 6.3: `catalog_list`, `catalog_item_<id>` y sus `_name_text`, `_price_text`, `_soldout_text`, `_add_button`, `product_image_<clave>`, `catalog_error_text`, los `product_detail_*`, `add_to_cart_sheet`, `add_to_cart_name_text`, `add_to_cart_quantity_stepper` (y `-Increment`/`-Decrement`), `add_to_cart_confirm_button`, `app_cart_full_alert_ok_button`, `app_snackbar_text` y `tab_cart` con la insignia.

Antes de empezar: `user-registration` está commiteado y archivado.

## 1. Imágenes

- [x] 1.1 Escribir `ProductImageTest` (falla): las seis claves (`headphones`, `keyboard`, `smartwatch`, `mouse`, `speaker`, `backpack`) devuelven un recurso distinto entre sí y distinto del respaldo; `"unknown"` y `""` devuelven `R.drawable.product_placeholder`; `"Headphones"` (otra mayúscula) también el respaldo. Verificar: UNIT falla por no compilar.
- [x] 1.2 Crear `scripts/svg_to_vector.py` (design.md §1) y generar `res/drawable/product_<clave>.xml` (6) y `product_placeholder.xml` desde `docs/imagenes-producto/`. Verificar: `./gradlew :app:assembleDebug` pasa (los XML son válidos) y `aapt2`/lint no se quejan.
- [x] 1.3 Crear `core/design/ProductImage.kt` (mapeo único) y `ProductImageView.kt` (ID `product_image_<imageKey>` con la clave recibida y "Sold out" opcional), y añadir a `TestTags` los de catálogo, detalle y hoja y a `AlertText` el de "Cart is full". Incluir un `@Preview` con las siete imágenes, con y sin "Sold out", en claro y oscuro. Verificar: `ProductImageTest` y `TestTagsTest` pasan, y los previews se ven iguales a los PNG de `docs/imagenes-producto/png/` en claro y oscuro (si no, corregir el script y regenerar).

## 2. Carrito (reglas y guardado)

- [x] 2.1 Escribir `CartRepositoryTest` (falla), con `InMemoryStore`: añadir un producto crea una línea; añadir otra vez suma a la misma (3 + 2 = 5 en una sola línea); tope de stock (stock 1 con 1 en el carrito: `maxAddable` 0 y `add` no cambia nada); tope de 20 (18 unidades: caben 2; `add(…, 5)` añade 2); `isFull` con 20; `totalQuantity`; la cantidad pedida mayor que lo que cabe se recorta; el carrito se recupera con un repositorio nuevo sobre el mismo almacén, con el precio intacto (`59.99`); un JSON ilegible da un carrito vacío sin lanzar. Verificar: UNIT falla por no compilar.
- [x] 2.2 Crear `data/model/CartItem.kt` y `data/repository/CartRepository.kt` (design.md §2) y `AppContainer.cart`. Verificar: `CartRepositoryTest` pasa con UNIT.
- [x] 2.3 Escribir `AddToCartActionTest` (falla): agotado → `Disabled`; con todo su stock en el carrito → `Disabled`; con stock y carrito de 20 → `CartFull`; con stock y carrito normal → `OpenSheet`; un producto con stock 5 y 4 en el carrito, y el carrito con 20 → `CartFull`. Crear `features/productdetail/AddToCartAction.kt`. Verificar: UNIT falla y después pasa.

## 3. Catálogo (datos y ViewModel)

- [x] 3.1 Escribir `CatalogIntegrationTest` (falla), con `CatalogViewModel` y backend real (**requiere backend**): seis productos en orden (`1..6`, de "Wireless Headphones" a "Laptop Backpack"), precio `59.99` y `25.00`, producto 3 agotado; error de carga con base URL `http://localhost:9/api/v1/` → `genericError` de red y `loadError` falso; `dismissGenericError()` → `loadError` verdadero; "Try again" (otro `load()` con backend disponible) → lista cargada y sin error; una recarga con datos no reactiva el indicador de pantalla completa. Crear `CatalogUiState.kt` y `CatalogViewModel.kt` (design.md §5) y su fábrica. Verificar: ALL falla por no compilar y después pasa.
- [x] 3.2 Escribir `ProductDetailIntegrationTest` (falla), con `ProductDetailViewModel` y backend real (**requiere backend**): producto 1 con su nombre, descripción, `59.99` y stock 10; producto 3 agotado; producto `99` → `genericError` con "This product is no longer available."; "Try again" repite la carga. Crear `ProductDetailUiState.kt` y `ProductDetailViewModel.kt` y su fábrica. Verificar: ALL falla y después pasa.

## 4. Pantallas

- [x] 4.1 Crear `features/productdetail/AddToCartFlow.kt` (estado de la hoja y de la alerta, design.md §3), `AddToCartSheet.kt` (hoja con `QuantityStepper`, design.md §4) y la alerta "Cart is full" con `testTagsAsResourceId` (§4 bis). Incluir `@Preview` de la hoja y de la alerta en claro y oscuro. Verificar: BUILD pasa.
- [x] 4.2 Crear `features/catalog/CatalogScreen.kt` (design.md §5): `PullToRefreshBox`, `LazyColumn` `catalog_list`, filas `catalog_item_<id>` sin `mergeDescendants`, "Sold out", `catalog_error_text` y recarga en `ON_RESUME`; el botón de la fila consume su toque. Incluir `@Preview` de una fila con stock, agotada y del estado de error. Verificar: BUILD pasa.
- [x] 4.3 Crear `features/productdetail/ProductDetailScreen.kt` (design.md §6) con el botón y la misma regla, y la barra superior con atrás. Incluir `@Preview` con y sin stock. Verificar: BUILD pasa.

## 5. Navegación e insignia

- [x] 5.1 Crear `app/Routes.kt` y convertir cada pestaña en un grafo con su pila (`catalog_graph` con `catalog` y `product/{id}`, `cart_graph`, `account_graph`), ajustando `Tab.route` y `MainTabs` (design.md §7). Verificar: BUILD pasa y, en el emulador, cambiar de pestaña conserva la pila de cada una (abrir un detalle, ir a Cart y volver: sigue el detalle).
- [x] 5.2 Mostrar la insignia del carrito (`BadgedBox`) con `cart.totalQuantity`, oculta con 0, y conectar `AddToCartFlow` con `container.cart` y `container.snackbar` en el catálogo y el detalle. Verificar: BUILD pasa.

## 6. Validación en el emulador (backend levantado, `reset` antes)

- [x] 6.1 Ejecutar ALL, BUILD y `assembleRelease`. Verificar: todo pasa, incluidos los de `bootstrap`, `app-shell`, `user-login` y `user-registration`.
- [x] 6.2 Entrando con `standard@shoplab.dev`, en claro y oscuro:
  - el catálogo muestra seis filas con sus imágenes y precios, y el producto 3 con "Sold out" y el botón deshabilitado;
  - tocar una fila abre el detalle (nombre, descripción, precio y "10 in stock"; el producto 3 "Sold out"); atrás vuelve a la lista;
  - el botón de la fila **no** abre el detalle;
  - "Add to cart" abre la hoja con el nombre y el selector (de 1 a las unidades que caben); añadir 3 muestra "Added to cart" 3 s y la insignia 3; cerrar la hoja sin confirmar no cambia nada;
  - añadir hasta 20 unidades en total: aparece "Cart is full" en lugar de la hoja; con todo el stock de un producto en el carrito su botón se deshabilita;
  - cerrar y abrir la app: la insignia conserva el total;
  - con el backend apagado (o `docker compose stop api`): la alerta genérica, "Cancel" deja `catalog_error_text` y deslizar para recargar lo recupera; "Try again" funciona;
  - pestañas: abrir un detalle, ir a Cart y volver conserva la pila; volver a Catalog desde otra pestaña recarga la lista.

  Verificar: cada caso se comporta como pide la spec; anotar lo que difiera y corregirlo.
- [x] 6.3 Revisar los IDs con `uiautomator dump` (los de la cabecera), en especial `product_image_<clave>` dentro de la fila, los hijos de `catalog_item_<id>`, la hoja y el selector (`-Increment`/`-Decrement`), y la alerta "Cart is full". Si alguno no llega, anotarlo en el catálogo (las tres copias) y en el plan A9. Verificar: los IDs se encuentran o hay nota.
- [x] 6.4 Actualizar el catálogo de IDs (nota de los botones `-Increment`/`-Decrement` del selector en Android, en las tres copias) y `docs/plan-iteraciones-android.md` (criterios de la A5). Verificar: casillas marcadas.

## Seguimiento

- El responsable revisa y hace el commit; después se archiva con `/opsx:archive`.

## Resultados de la verificación

- 173 tests JVM pasan (incluidos los de integración contra el backend real); `assembleDebug`, `assembleRelease` y `lintDebug` sin errores.
- Emulador (Pixel 6a, Android 17): seis filas con imagen, nombre y precio; el producto 3 con "Sold out" y el botón deshabilitado; tocar una fila abre el detalle (nombre, descripción, `$59.99`, "10 in stock"; el 3 sin stock y con el botón deshabilitado) y atrás vuelve a la lista; el botón de la fila no abre el detalle; la hoja muestra el nombre y el selector (el "−" deshabilitado en 1, el "+" deshabilitado en el máximo), añadir 3 muestra "Added to cart" durante 3 s y la insignia; con 20 unidades aparece "Cart is full" en lugar de la hoja; con todo el stock de un producto en el carrito su botón queda deshabilitado; cerrar y abrir la app conserva el carrito (insignia 20); con el backend apagado: alerta genérica, "Cancel" deja `catalog_error_text` y deslizar para recargar recupera la lista; abrir un detalle, ir a Cart y volver conserva la pila de Catalog; claro y oscuro legibles.
- IDs (`uiautomator dump`): llegan `catalog_list`, `catalog_item_<id>` **y los de sus hijos** (nombre, precio, "Sold out", botón y `product_image_<clave>`), los `product_detail_*`, `add_to_cart_sheet`, `add_to_cart_name_text`, el selector con `-Increment` y `-Decrement`, `add_to_cart_confirm_button`, `app_cart_full_alert_ok_button`, `app_snackbar_text` y `tab_cart`. La insignia no expone su texto a `uiautomator` (se comprueba a la vista).
- **Hallazgos:**
  - `Modifier.clickable` fusiona la semántica de los hijos y hace desaparecer sus IDs: se creó `clickableKeepingChildren` (pointerInput + `onClick` sin `mergeDescendants`).
  - Las filas del catálogo no se redibujaban al cambiar el carrito: las acciones de los botones se calculan donde se lee el carrito, fuera de la lista.
  - Los vectores generados con `scripts/svg_to_vector.py` se ven iguales a los SVG, también en grande (detalle) y en oscuro.
