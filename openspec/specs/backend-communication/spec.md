# backend-communication Specification

## Purpose
Define cómo la app se comunica con el backend ShopLab: a qué dirección llama, qué envía en cada petición, cómo lee fechas y montos, y cómo convierte cada respuesta fallida en un error que el resto de la app entiende.

## Requirements

### Requirement: Base URL configurable
La app SHALL tomar la base URL de `BuildConfig.API_BASE_URL`: en debug `http://10.0.2.2:8080/api/v1/` (el backend de la máquina de desarrollo visto desde el emulador) y en release la URL de producción. El `AppContainer` SHALL aceptar otra URL para los tests. Cambiarla por un extra de arranque llega en la iteración de pruebas móviles.

#### Scenario: Compilación debug
- **WHEN** se compila la variante debug
- **THEN** `BuildConfig.API_BASE_URL` es `http://10.0.2.2:8080/api/v1/`

#### Scenario: Otra URL en un test
- **WHEN** un test crea el `AppContainer` con `http://localhost:8080/api/v1/`
- **THEN** las peticiones van a esa dirección

### Requirement: Cabeceras de la petición
Una petición con body SHALL enviar `Content-Type: application/json`. Una petición a una ruta protegida SHALL enviar `Authorization: Bearer <token>` cuando hay sesión. Una ruta pública MUST NOT enviar el token.

#### Scenario: Petición con body
- **WHEN** la app envía un body JSON
- **THEN** la petición lleva `Content-Type: application/json` y el backend no responde 415

#### Scenario: Ruta protegida con sesión
- **WHEN** hay sesión y la app pide `GET /users/me`
- **THEN** la petición lleva `Authorization: Bearer` con el token de la sesión

### Requirement: Tiempo de espera
Cada petición SHALL esperar la respuesta hasta 15 segundos, suficiente para el retraso máximo de 10 s de `X-Delay` del perfil `testing`. Pasado ese tiempo MUST tratarse como un error de conexión.

#### Scenario: Respuesta lenta dentro del límite
- **WHEN** el backend tarda 10 segundos en responder
- **THEN** la app recibe la respuesta normalmente

### Requirement: Error de conexión
Si la petición no llega a recibir una respuesta HTTP (sin red, servidor apagado o tiempo agotado), la app SHALL tratarla como un error de conexión. Una cancelación de la corrutina MUST relanzarse y no convertirse en error.

#### Scenario: Servidor inaccesible
- **WHEN** la base URL apunta a un puerto donde no escucha nada
- **THEN** la petición termina con un error de conexión

### Requirement: Errores del backend con su código
Toda respuesta 4xx o 5xx SHALL convertirse en un error que conserva el status HTTP, el `code` y el `message` del formato único de error del backend. Si el body no se puede leer, el `code` MUST ser `UNKNOWN` y se conserva el status.

#### Scenario: Producto inexistente
- **WHEN** la app pide `GET /products/99`
- **THEN** obtiene un error con status 404 y `code` `PRODUCT_NOT_FOUND`

#### Scenario: Error forzado de servidor
- **WHEN** el backend responde 503 con `code` `FORCED_ERROR`
- **THEN** obtiene un error con status 503 y `code` `FORCED_ERROR`

### Requirement: Errores de campo agrupados por campo
En un `VALIDATION_ERROR`, la app SHALL agrupar los `fieldErrors` por `field` y conservar todos los mensajes de cada campo, en el orden en que llegan.

#### Scenario: Body vacío en el registro
- **WHEN** la app envía `POST /users` con `{}`
- **THEN** el error tiene `code` `VALIDATION_ERROR` y un grupo por cada campo obligatorio (`birthDate`, `country`, `email`, `experienceLevel`, `firstName`, `lastName`, `password`, `plan`, `termsAccepted`)

#### Scenario: Un campo con dos mensajes
- **WHEN** la app envía `POST /users` con `password` `abc`
- **THEN** el grupo de `password` contiene dos mensajes

### Requirement: Respuesta exitosa ilegible
Si una respuesta 2xx no tiene la forma esperada, la app SHALL tratarla como un error desconocido.

#### Scenario: JSON inesperado
- **WHEN** una respuesta 200 no coincide con el modelo esperado
- **THEN** la petición termina con un error desconocido, no con un cierre de la app

### Requirement: Montos exactos
La app SHALL representar los montos (`price`, `unitPrice`, `subtotal`, `total`) como decimales exactos (`BigDecimal`) redondeados a centavos, nunca como coma flotante, y SHALL mostrarlos en formato USD en inglés.

#### Scenario: Precio con decimales
- **WHEN** llega el precio `59.99`
- **THEN** el monto es exactamente `59.99` y se muestra como `$59.99`

#### Scenario: Precio con ceros
- **WHEN** llega el precio `25.00`
- **THEN** se muestra como `$25.00`

### Requirement: Fechas con hora del backend
La app SHALL leer `createdAt` en UTC con 0 a 9 decimales de segundo, conservando al menos los milisegundos. Un texto que no sea una fecha válida MUST hacer fallar la lectura de esa respuesta, que termina como error desconocido.

#### Scenario: Sin decimales
- **WHEN** llega `2026-10-14T15:04:05Z`
- **THEN** se lee como el 14 de octubre de 2026, 15:04:05 UTC

#### Scenario: Con seis o nueve decimales
- **WHEN** llega `2026-10-14T15:04:05.123456Z` o `2026-10-14T15:04:05.123456789Z`
- **THEN** se lee como 15:04:05.123 UTC o con más precisión, sin perder el instante

#### Scenario: Texto inválido
- **WHEN** llega `not-a-date`
- **THEN** la lectura de la respuesta falla

### Requirement: Formato de las fechas que ve el usuario
Las fechas SHALL mostrarse en inglés de Estados Unidos y en la zona horaria del dispositivo: `Sep 15, 2026` en las listas y `Sep 15, 2026, 11:30 AM` (hora de 12 h con AM/PM y un espacio normal antes) en el detalle. Las unidades SHALL mostrarse como "1 item" o "N items".

#### Scenario: Zona del dispositivo
- **WHEN** llega `2026-09-01T03:00:00Z` y el dispositivo está en Bogotá (UTC-5)
- **THEN** la lista muestra `Aug 31, 2026` y el detalle `Aug 31, 2026, 10:00 PM`

#### Scenario: Cambio de día en otra zona
- **WHEN** llega la misma fecha y el dispositivo está en Auckland (UTC+12)
- **THEN** la lista muestra `Sep 1, 2026` y el detalle `Sep 1, 2026, 3:00 PM`

#### Scenario: Unidades
- **WHEN** una orden tiene 1 unidad o 3 unidades
- **THEN** se muestra "1 item" o "3 items"
