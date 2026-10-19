# cart-screen Specification

## Purpose
Define la pantalla del carrito: qué se ve en cada línea y en el total, cómo se editan las cantidades, cómo se quita y se vacía, el estado vacío, el acceso al checkout y cómo el carrito se ajusta al stock real.

## Requirements

### Requirement: Líneas y total del carrito
La pestaña Cart SHALL mostrar una línea por cada producto del carrito, con su imagen, nombre, selector de cantidad y subtotal estimado (precio × cantidad, por ejemplo `$119.98`), y debajo el total estimado, que es la suma de los subtotales. Las líneas MUST aparecer en el orden en que se añadieron.

#### Scenario: Dos productos
- **WHEN** el carrito tiene 2 unidades del producto 1 (`$59.99`) y 1 del producto 4 (`$25.00`)
- **THEN** se ven las dos líneas con subtotales `$119.98` y `$25.00` y el total `$144.98`

### Requirement: Editar la cantidad de una línea
El selector de cada línea (botones "−" y "+" con el valor en medio) SHALL permitir cantidades de 1 hasta el menor entre el stock del producto y las unidades que ya tiene la línea más las que falten para llegar a 20 en total. Al cambiarla, el subtotal, el total y la insignia de la pestaña MUST actualizarse y el cambio MUST conservarse al cerrar la app.

#### Scenario: Tope por stock
- **WHEN** un producto con stock 3 está en el carrito con 3 unidades
- **THEN** el selector no sube de 3

#### Scenario: Tope de 20
- **WHEN** el carrito tiene 18 unidades en total y la línea editada tiene 2, con stock de sobra
- **THEN** el selector llega como máximo a 4

#### Scenario: Cantidad mínima
- **WHEN** una línea tiene 1 unidad
- **THEN** el selector no baja de 1 (para quitar la línea se usa "Delete")

### Requirement: Quitar una línea
Al deslizar una línea hacia la izquierda, la app SHALL mostrar el botón "Delete", que quita esa línea del carrito. Soltar la línea a medio camino MUST dejarla abierta o cerrada según haya pasado la mitad. El total y la insignia MUST actualizarse y las demás líneas MUST NOT cambiar.

#### Scenario: Eliminar una línea
- **WHEN** el usuario desliza la línea del producto 1 y pulsa "Delete"
- **THEN** esa línea desaparece, y el total y la insignia bajan

### Requirement: Vaciar el carrito
El botón "Clear cart" SHALL pedir confirmación con el diálogo "Remove all items from your cart?", con "Clear cart" (destructivo) y "Cancel". Al confirmar, el carrito MUST quedar vacío; al cancelar, MUST NOT cambiar.

#### Scenario: Confirmar
- **WHEN** el usuario pulsa "Clear cart" y luego confirma "Clear cart"
- **THEN** el carrito queda vacío y se ve el estado vacío

#### Scenario: Cancelar
- **WHEN** el usuario pulsa "Clear cart" y luego "Cancel"
- **THEN** el carrito conserva todas sus líneas

### Requirement: Carrito vacío y acceso al checkout
Con el carrito vacío, la pantalla SHALL mostrar el texto de estado vacío y MUST NOT mostrar el botón "Checkout". Con al menos una línea, SHALL mostrar el botón "Checkout", que abre la pantalla de checkout (provisional hasta que exista el checkout real).

#### Scenario: Sin productos
- **WHEN** el carrito no tiene líneas
- **THEN** se ve el estado vacío y no existe el botón "Checkout"

#### Scenario: Con productos
- **WHEN** el carrito tiene al menos una línea
- **THEN** existe el botón "Checkout" y al pulsarlo se abre el checkout

### Requirement: Ajuste al stock real
Cada vez que la pantalla del carrito aparece, la app SHALL pedir los productos al backend y ajustar el carrito: bajar cada cantidad al stock disponible y quitar las líneas de productos agotados o que ya no existen. Si algo cambió, SHALL mostrar el snackbar "Your cart was updated to match available stock."; si nada cambió, MUST NOT mostrarlo.

#### Scenario: Producto agotado
- **WHEN** el carrito guardado tiene el producto 3 (stock 0) y el usuario abre la pestaña Cart
- **THEN** esa línea desaparece y aparece el snackbar

#### Scenario: Cantidad mayor que el stock
- **WHEN** el carrito tiene 5 unidades de un producto con stock 1
- **THEN** su cantidad baja a 1 y aparece el snackbar

#### Scenario: Sin cambios
- **WHEN** el carrito tiene 1 unidad del producto 5 y hay stock suficiente
- **THEN** el carrito no cambia y no aparece el snackbar

### Requirement: Error al ajustar el carrito
Si la petición de productos falla, la app SHALL mostrar la alerta genérica y el carrito MUST NOT modificarse. "Try again" SHALL repetir el ajuste.

#### Scenario: Backend no disponible
- **WHEN** el usuario abre la pestaña Cart y la petición falla
- **THEN** aparece la alerta genérica y el carrito conserva sus líneas y cantidades
