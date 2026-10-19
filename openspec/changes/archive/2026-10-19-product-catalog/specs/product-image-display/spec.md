# Spec Delta

## Purpose
Define qué imagen se muestra para cada producto y cómo se identifica, para que el catálogo, el detalle, el carrito y las órdenes se vean igual y nunca queden sin imagen.

## ADDED Requirements

### Requirement: Una imagen por clave de producto
La app SHALL llevar empaquetada una imagen vectorial cuadrada propia para cada `imageKey` conocido (`headphones`, `keyboard`, `smartwatch`, `mouse`, `speaker` y `backpack`) y SHALL mostrar siempre la que corresponde a la clave del producto. Las imágenes MUST verse completas y nítidas en cualquier tamaño y en modo claro y oscuro.

#### Scenario: Clave conocida
- **WHEN** se muestra un producto con `imageKey` `headphones`
- **THEN** se ve la imagen de los audífonos, distinta de las de las otras cinco claves

#### Scenario: Modo oscuro
- **WHEN** el sistema está en modo oscuro
- **THEN** la imagen se distingue del fondo y no queda recortada

### Requirement: Imagen de respaldo
Si un producto llega con un `imageKey` que la app no conoce, SHALL mostrarse una imagen genérica de respaldo. La imagen MUST NOT faltar ni quedar vacía, y la app MUST NOT fallar.

#### Scenario: Clave desconocida
- **WHEN** se muestra un producto con `imageKey` `unknown`
- **THEN** se ve la imagen de respaldo

#### Scenario: Clave vacía
- **WHEN** el `imageKey` es una cadena vacía
- **THEN** se ve la imagen de respaldo

### Requirement: Identificador de la imagen
Cada imagen mostrada SHALL llevar el identificador `product_image_<imageKey>` con la clave del producto (por ejemplo `product_image_headphones`), también cuando se usa la imagen de respaldo con una clave desconocida, para que las pruebas automáticas comprueben qué imagen se muestra.

#### Scenario: Clave conocida
- **WHEN** se muestra un producto con `imageKey` `mouse`
- **THEN** la imagen es localizable por `product_image_mouse`

#### Scenario: Clave desconocida
- **WHEN** se muestra un producto con `imageKey` `unknown`
- **THEN** la imagen de respaldo es localizable por `product_image_unknown`

### Requirement: Etiqueta "Sold out"
Cuando el producto está agotado (`stock` 0), la app SHALL superponer la etiqueta "Sold out" sobre su imagen, sin cambiar la imagen. La etiqueta MUST NOT existir si el producto tiene stock, y su identificador SHALL ser el que indique la pantalla que la muestra.

#### Scenario: Producto agotado
- **WHEN** se muestra un producto con `stock` 0
- **THEN** su imagen lleva encima la etiqueta "Sold out"

#### Scenario: Producto con stock
- **WHEN** se muestra un producto con `stock` mayor que 0
- **THEN** no existe la etiqueta "Sold out"
