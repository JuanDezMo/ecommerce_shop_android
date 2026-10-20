# Spec Delta

## Purpose
Define el detalle de una orden: los datos que muestra, tomados del backend, y el caso de una orden inexistente.

## ADDED Requirements

### Requirement: Cabecera de la orden
El detalle SHALL mostrar "Order #N", el estado ("Confirmed"), la fecha y la hora de creación (la fecha en el mismo formato de la lista, más la hora, en la zona del dispositivo) y la forma de pago ("Card", "Cash" o "Transfer").

#### Scenario: Orden 1 de buyer
- **WHEN** `buyer@shoplab.dev` abre la orden 1
- **THEN** ve "Order #1", "Confirmed", la fecha `Sep 1, 2026` con su hora y la forma de pago

### Requirement: Productos de la orden
El detalle SHALL mostrar cada producto, en el orden del backend, con su imagen, nombre, la cantidad por el precio unitario (por ejemplo `2 × $25.00`) y su subtotal.

#### Scenario: Dos líneas
- **WHEN** se abre la orden 1 de `buyer@shoplab.dev`
- **THEN** ve dos líneas, con subtotales `$59.99` y `$50.00`

### Requirement: Total y envío
El detalle SHALL mostrar el total tal como lo devuelve el backend (sin sumar en la app) y la dirección de envío con la dirección, la ciudad, el código postal y el país en líneas separadas.

#### Scenario: Total del backend
- **WHEN** se abre la orden 1 de `buyer@shoplab.dev`
- **THEN** el total es `$109.99` y el envío muestra "Calle 10 #20-30", "Bogotá", "110111" y el país

### Requirement: Orden inexistente o ajena
Si el backend responde 404 `ORDER_NOT_FOUND` (la orden no existe o es de otro usuario), la app SHALL mostrar la alerta "Order not found" con el texto "We couldn't find this order." y el botón "OK", que SHALL volver a la pantalla anterior, sea la lista o la confirmación.

#### Scenario: Orden de otro usuario
- **WHEN** `standard@shoplab.dev` abre la orden 1, que es de `buyer@shoplab.dev`
- **THEN** aparece "Order not found" y "OK" vuelve a la pantalla anterior

### Requirement: Otros errores del detalle
Ante cualquier otro error al cargar, la app SHALL mostrar la alerta genérica y "Try again" SHALL repetir la carga.

#### Scenario: Backend no disponible
- **WHEN** se abre el detalle y la petición falla por red
- **THEN** aparece la alerta genérica y "Try again" repite la carga
