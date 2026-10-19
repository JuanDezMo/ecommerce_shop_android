# user-error-messages Specification

## Purpose
Define el texto en inglés que ve el usuario para cada error, elegido por el `code` del backend o por el tipo de fallo, para que los mensajes sean claros y estables para las suites de pruebas.

## Requirements

### Requirement: Nunca el mensaje técnico del backend
El texto que ve el usuario SHALL salir de una tabla de mensajes de la app. El `message` del backend MUST NOT mostrarse al usuario; solo puede usarse en logs.

#### Scenario: Producto inexistente
- **WHEN** el backend responde 404 `PRODUCT_NOT_FOUND` con `message` `Product 99 not found`
- **THEN** el mensaje para el usuario es "This product is no longer available." y no contiene "Product 99 not found"

### Requirement: Mensaje por código de negocio
Cada `code` de negocio conocido del backend SHALL tener un mensaje fijo en inglés. Los escenarios fijan el texto exacto de cada uno.

#### Scenario: Códigos de cuenta y validación
- **WHEN** el error tiene uno de estos `code`
- **THEN** el mensaje es el de la tabla:

| `code` | Mensaje |
| --- | --- |
| `VALIDATION_ERROR` | "Some fields need your attention." |
| `INVALID_CREDENTIALS` | "The email or password is incorrect." |
| `USER_LOCKED` | "Your account is locked. Please contact support." |
| `EMAIL_ALREADY_EXISTS` | "An account with this email already exists." |
| `UNDERAGE_USER` | "You must be at least 18 years old to register." |
| `TERMS_NOT_ACCEPTED` | "You must accept the terms and conditions." |

#### Scenario: Códigos de productos y órdenes
- **WHEN** el error tiene uno de estos `code`
- **THEN** el mensaje es el de la tabla:

| `code` | Mensaje |
| --- | --- |
| `PRODUCT_NOT_FOUND` | "This product is no longer available." |
| `INSUFFICIENT_STOCK` | "Some products in your cart don't have enough stock. Please update the quantities." |
| `EMPTY_ORDER` | "Your cart is empty." |
| `DUPLICATE_ORDER_ITEM` | "We couldn't process your cart. Please try again." |
| `ORDER_NOT_FOUND` | "We couldn't find this order." |

### Requirement: Mensaje genérico por familia de status
Un error con un `code` sin mensaje propio (incluidos `FORCED_ERROR`, `NOT_FOUND`, `METHOD_NOT_ALLOWED`, `UNSUPPORTED_MEDIA_TYPE`, `INTERNAL_ERROR` y `UNKNOWN`) SHALL mostrar "Something went wrong. Please try again." si el status es 4xx, y "Something went wrong on our side. Please try again later." si es 5xx.

#### Scenario: Error forzado 4xx
- **WHEN** el error es 418 con `code` `FORCED_ERROR`
- **THEN** el mensaje es "Something went wrong. Please try again."

#### Scenario: Error forzado 5xx
- **WHEN** el error es 503 con `code` `FORCED_ERROR`
- **THEN** el mensaje es "Something went wrong on our side. Please try again later."

### Requirement: Mensajes de fallos sin código
Los fallos que no traen un `code` del backend (conexión, sesión expirada, no autenticado y error desconocido) SHALL tener cada uno su propio mensaje fijo.

#### Scenario: Sin conexión
- **WHEN** la petición termina con un error de conexión
- **THEN** el mensaje es "Check your internet connection and try again."

#### Scenario: Sesión expirada
- **WHEN** la petición termina con sesión expirada
- **THEN** el mensaje es "Your session has expired. Please log in again."

#### Scenario: No autenticado
- **WHEN** la petición termina con no autenticado
- **THEN** el mensaje es "Please log in to continue."

#### Scenario: Error desconocido
- **WHEN** la petición termina con un error desconocido
- **THEN** el mensaje es "Something unexpected happened. Please try again."
