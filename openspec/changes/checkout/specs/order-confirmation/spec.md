# Spec Delta

## Purpose
Define la pantalla que cierra la compra: qué datos se muestran y adónde lleva cada acción.

## ADDED Requirements

### Requirement: Datos de la confirmación
La confirmación SHALL mostrar el número de la orden, una línea por cada producto comprado (con su nombre, cantidad y subtotal) y el total, todos tomados de la respuesta del backend, sin hacer otra consulta. La pantalla MUST NOT tener botón de atrás ni permitir volver con el gesto o el botón de atrás del sistema.

#### Scenario: Sin volver atrás
- **WHEN** el usuario usa el gesto o el botón de atrás del sistema en la confirmación
- **THEN** la confirmación sigue en pantalla

#### Scenario: Orden de dos productos
- **WHEN** se muestra la confirmación de una orden con el producto 1 (2 unidades) y el producto 4 (1 unidad)
- **THEN** se ven el número de orden, las dos líneas y el total `$144.98` del backend

### Requirement: Ver la orden
El botón "View order" SHALL abrir el detalle de esa orden (una pantalla provisional con su número hasta que exista el detalle real).

#### Scenario: Abrir el detalle
- **WHEN** el usuario pulsa "View order"
- **THEN** se abre el detalle de la orden recién creada

### Requirement: Terminar la compra
El botón "Done" SHALL cerrar el flujo de compra y cambiar a la pestaña Catalog, que se recarga y muestra el stock actualizado.

#### Scenario: Stock actualizado
- **WHEN** el usuario compró 2 unidades del producto 1 (stock 10) y pulsa "Done"
- **THEN** ve el catálogo, y el producto 1 muestra 8 unidades en su detalle
