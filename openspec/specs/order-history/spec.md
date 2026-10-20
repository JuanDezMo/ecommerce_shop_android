# order-history Specification

## Purpose
Define la lista "My orders": qué órdenes muestra, en qué orden, cómo se ve cada fila y qué pasa cuando no hay órdenes o falla la carga.

## Requirements

### Requirement: Lista de órdenes
"My orders" SHALL mostrar las órdenes del usuario con sesión, en el orden en que las devuelve el backend (la más reciente primero). Cada fila SHALL mostrar "Order #N", la fecha (por ejemplo `Sep 15, 2026`, en la zona del dispositivo), el número de unidades (la suma de las cantidades de la orden: "1 item" o "N items") y el total del backend (por ejemplo `$45.00`). Mientras carga SHALL verse el indicador de carga.

#### Scenario: Usuario con dos órdenes
- **WHEN** `buyer@shoplab.dev` abre "My orders"
- **THEN** ve la orden 2 (`$45.00`, "1 item") y después la orden 1 (`$109.99`, "3 items")

#### Scenario: Compra reciente
- **WHEN** el usuario acaba de comprar y abre "My orders"
- **THEN** su compra aparece la primera

### Requirement: Estado vacío
Si el usuario no tiene órdenes, la pantalla SHALL mostrar "You haven't placed any orders yet." y MUST NOT mostrar filas.

#### Scenario: Usuario sin órdenes
- **WHEN** `standard@shoplab.dev`, sin compras, abre "My orders"
- **THEN** ve "You haven't placed any orders yet."

### Requirement: Recarga
La lista SHALL recargarse cada vez que la pantalla aparece y al deslizar para recargar.

#### Scenario: Volver desde el detalle
- **WHEN** el usuario compra, vuelve a Account y abre "My orders"
- **THEN** la lista incluye la compra nueva sin reiniciar la app

### Requirement: Abrir el detalle
Al tocar una fila, la app SHALL abrir el detalle de esa orden, y el botón de atrás SHALL volver a la lista.

#### Scenario: Tocar una fila
- **WHEN** el usuario toca la fila de la orden 1
- **THEN** se abre el detalle de la orden 1

### Requirement: Error al cargar las órdenes
Si la carga falla, la app SHALL mostrar la alerta genérica y "Try again" SHALL repetir la carga.

#### Scenario: Backend no disponible
- **WHEN** el usuario abre "My orders" y la petición falla
- **THEN** aparece la alerta genérica y "Try again" repite la carga
