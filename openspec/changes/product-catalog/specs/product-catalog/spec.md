# Spec Delta

## Purpose
Define qué ve y qué puede hacer el usuario al explorar los productos: la lista del catálogo, el detalle de cada producto, los productos agotados y los errores de carga.

## ADDED Requirements

### Requirement: Lista de productos
La pestaña Catalog SHALL mostrar los productos del backend en el orden en que llegan, cada uno en una fila con su imagen, nombre y precio (por ejemplo `$59.99`) y su botón "Add to cart". La lista MUST recargarse cada vez que la pantalla aparece y al deslizar para recargar, y mientras carga SHALL verse el indicador de carga.

#### Scenario: Seis productos
- **WHEN** el usuario abre la pestaña Catalog con el backend con sus datos semilla
- **THEN** ve seis filas, de "Wireless Headphones" a "Laptop Backpack", y la primera con el precio `$59.99`

#### Scenario: Recarga al volver
- **WHEN** el usuario vuelve a la pestaña Catalog desde otra pestaña
- **THEN** la lista se recarga con los datos actuales del backend

### Requirement: Producto agotado
Un producto con `stock` 0 SHALL mostrar la etiqueta "Sold out" sobre su imagen y su botón "Add to cart" MUST estar deshabilitado. Las demás filas MUST NOT mostrar la etiqueta.

#### Scenario: Smart Watch agotado
- **WHEN** se muestra el producto 3 (stock 0)
- **THEN** su fila lleva "Sold out" y su botón "Add to cart" está deshabilitado

### Requirement: Detalle de producto
Al tocar una fila, la app SHALL abrir el detalle del producto, pedido al backend, con su imagen, nombre, descripción, precio y stock ("10 in stock" o "1 in stock"; con stock 0, "Sold out" en su lugar), y el botón "Add to cart" con las mismas reglas que el de la fila. El botón de atrás (y el gesto de atrás del sistema) MUST volver a la lista, y la barra de pestañas MUST seguir visible en el detalle.

#### Scenario: Producto con stock
- **WHEN** el usuario toca la fila del producto 1
- **THEN** ve su nombre, descripción, `$59.99` y "10 in stock"

#### Scenario: Producto agotado
- **WHEN** el usuario abre el detalle del producto 3
- **THEN** ve "Sold out" en lugar del stock y el botón "Add to cart" deshabilitado

### Requirement: Errores al cargar el catálogo
Si la carga de la lista falla, la app SHALL mostrar la alerta genérica. "Try again" SHALL repetir la carga. "Cancel" SHALL dejar la lista con el texto "We couldn't load the products. Pull to refresh.".

#### Scenario: Cancelar tras un error
- **WHEN** la carga falla y el usuario pulsa "Cancel"
- **THEN** la lista muestra "We couldn't load the products. Pull to refresh."

#### Scenario: Reintentar
- **WHEN** la carga falla y el usuario pulsa "Try again" con el backend ya disponible
- **THEN** la lista se carga y no queda ningún error

### Requirement: Errores al cargar el detalle
Si el producto ya no existe (404 `PRODUCT_NOT_FOUND`), la app SHALL mostrar la alerta genérica con el mensaje "This product is no longer available.". Cualquier otro error SHALL mostrar la alerta genérica con su mensaje habitual. "Try again" SHALL repetir la carga.

#### Scenario: Producto inexistente
- **WHEN** se abre el detalle de un producto que el backend ya no tiene
- **THEN** la alerta dice "This product is no longer available."
