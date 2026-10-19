# Spec Delta

## Purpose
Define cómo se añade un producto al carrito y las reglas que el carrito cumple siempre: una línea por producto, máximo 20 unidades y nunca más que el stock. Es la base de la pantalla del carrito y del checkout.

## ADDED Requirements

### Requirement: Límites del carrito
El carrito SHALL tener una sola línea por producto y como máximo 20 unidades en total. Las unidades de un producto MUST NOT superar su stock. Las unidades que caben de un producto son el menor entre su stock menos lo que ya hay en el carrito y 20 menos el total, y nunca menos de cero.

#### Scenario: Más unidades del mismo producto
- **WHEN** se añaden 2 unidades de un producto que ya tiene 3 en el carrito
- **THEN** el carrito tiene una sola línea de ese producto, con 5 unidades

#### Scenario: Tope por stock
- **WHEN** un producto con stock 1 ya tiene 1 unidad en el carrito
- **THEN** no cabe ninguna unidad más de ese producto

#### Scenario: Tope de 20
- **WHEN** el carrito tiene 18 unidades y un producto tiene stock de sobra
- **THEN** de ese producto caben 2 unidades más

### Requirement: Botón "Add to cart"
El botón "Add to cart", en la fila y en el detalle, SHALL estar deshabilitado si el producto está agotado o si sus unidades en el carrito ya igualan su stock. Si está habilitado y el carrito ya tiene 20 unidades, SHALL mostrar la alerta "Cart is full" ("Your cart can hold up to 20 items.", botón "OK") en lugar de la hoja. En los demás casos SHALL abrir la hoja "Add to cart".

#### Scenario: Carrito lleno
- **WHEN** el carrito tiene 20 unidades y el usuario pulsa "Add to cart" de un producto con stock disponible
- **THEN** aparece la alerta "Cart is full" y no se abre la hoja

#### Scenario: Stock agotado en el carrito
- **WHEN** el carrito ya tiene todo el stock de un producto
- **THEN** su botón "Add to cart" está deshabilitado

### Requirement: Hoja "Add to cart"
La hoja SHALL mostrar el nombre del producto, un selector de cantidad (botones "−" y "+" con el valor en medio) de 1 hasta las unidades que caben, y el botón "Add to cart". Cerrarla sin confirmar MUST NOT añadir nada. Al confirmar, la app SHALL añadir esa cantidad al carrito, cerrar la hoja y mostrar durante 3 segundos el snackbar "Added to cart".

#### Scenario: Añadir tres unidades
- **WHEN** el usuario elige 3 y pulsa "Add to cart" en la hoja
- **THEN** el carrito suma 3 unidades, la hoja se cierra y aparece "Added to cart"

#### Scenario: Cerrar sin confirmar
- **WHEN** el usuario abre la hoja y la cierra deslizándola o con el gesto de atrás
- **THEN** el carrito no cambia y no aparece el snackbar

#### Scenario: Cantidad máxima
- **WHEN** el producto tiene stock 10, hay 8 en el carrito y 12 en total
- **THEN** el selector llega como máximo a 2

### Requirement: Insignia del carrito
La pestaña Cart SHALL mostrar como insignia el total de unidades del carrito y no mostrarla cuando está vacío.

#### Scenario: Total de unidades
- **WHEN** el carrito tiene 2 unidades de un producto y 3 de otro
- **THEN** la pestaña Cart muestra la insignia 5

### Requirement: El carrito se conserva
El carrito SHALL guardarse en el dispositivo y conservarse al cerrar y volver a abrir la app.

#### Scenario: Reabrir la app
- **WHEN** el carrito tiene productos y la app se cierra y se abre de nuevo
- **THEN** el carrito conserva las mismas líneas y cantidades
