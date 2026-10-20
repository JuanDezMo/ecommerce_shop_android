# user-account Specification

## Purpose
Define la pestaña Account: qué datos del usuario muestra, el acceso a sus órdenes y el cierre de sesión con su confirmación y sus efectos.

## Requirements

### Requirement: Perfil del usuario
La pestaña Account SHALL mostrar el nombre completo, el email y el plan ("Basic" o "Pro") del usuario con sesión, pedidos al backend. Los datos MUST recargarse cada vez que la pestaña aparece.

#### Scenario: Usuario semilla
- **WHEN** el usuario `standard@shoplab.dev` abre la pestaña Account
- **THEN** ve su nombre completo que empieza por "Standard", el email `standard@shoplab.dev` y su plan

### Requirement: Error al cargar el perfil
Si la carga del perfil falla, la app SHALL mostrar la alerta genérica y "Try again" SHALL repetir la carga. Si el token expiró, la app SHALL cerrar la sesión y volver al login con el aviso de sesión expirada, conservando el carrito.

#### Scenario: Backend no disponible
- **WHEN** el usuario abre la pestaña Account y la petición falla
- **THEN** aparece la alerta genérica y "Try again" repite la carga

#### Scenario: Token expirado
- **WHEN** el token expiró y el usuario abre la pestaña Account
- **THEN** se vuelve al login con "Your session has expired. Please log in again." y el carrito conserva sus líneas

### Requirement: Acceso a las órdenes
La pestaña Account SHALL tener el botón "My orders", que abre la lista de órdenes del usuario.

#### Scenario: Abrir las órdenes
- **WHEN** el usuario pulsa "My orders"
- **THEN** se abre la pantalla de órdenes y el botón de atrás vuelve a Account

### Requirement: Cerrar sesión con confirmación
El botón "Log out" SHALL abrir la alerta "Log out?" con el texto "Your cart will be emptied." y los botones "Log out" (destructivo) y "Cancel". "Cancel" SHALL cerrar la alerta sin cambios.

#### Scenario: Cancelar
- **WHEN** el usuario pulsa "Log out" y luego "Cancel"
- **THEN** sigue en la pestaña Account con su sesión y su carrito intactos

### Requirement: Efectos del cierre de sesión
Al confirmar, la app SHALL vaciar el carrito, terminar la sesión sin aviso de expiración y mostrar el login. Cuando el usuario vuelva a iniciar sesión, la app MUST abrir la pestaña Catalog, sin pantallas apiladas de la sesión anterior en ninguna pestaña.

#### Scenario: Cerrar sesión con carrito
- **WHEN** el usuario con 3 unidades en el carrito confirma "Log out"
- **THEN** ve el login sin el aviso de sesión expirada y, al volver a entrar, el carrito está vacío y la pestaña visible es Catalog
