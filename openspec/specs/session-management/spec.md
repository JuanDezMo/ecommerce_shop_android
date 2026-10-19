# session-management Specification

## Purpose
Define el estado de la sesión del usuario en la app (iniciada, terminada o expirada), dónde vive el token y cómo reacciona la app cuando el backend rechaza el token.

## Requirements

### Requirement: Token solo en memoria
El token de acceso SHALL guardarse solo en memoria mientras la app está abierta. MUST NOT guardarse en el disco: al cerrar y volver a abrir la app no hay sesión.

#### Scenario: Reabrir la app
- **WHEN** el usuario inicia sesión, cierra la app y la vuelve a abrir
- **THEN** la app no tiene sesión

### Requirement: Iniciar sesión
Al recibir un token, la app SHALL quedar con sesión iniciada y MUST borrar cualquier marca previa de sesión expirada.

#### Scenario: Inicio tras una expiración
- **WHEN** la sesión había expirado y se inicia una sesión nueva
- **THEN** hay sesión y la marca de expiración desaparece

### Requirement: Terminar la sesión
Al terminar la sesión (logout del usuario), la app SHALL borrar el token y quedar sin sesión, sin marca de expiración.

#### Scenario: Logout
- **WHEN** se termina una sesión iniciada
- **THEN** no hay token, no hay sesión y no hay marca de expiración

### Requirement: Sesión expirada por el backend
Si una ruta protegida responde 401 con `code` `AUTH_TOKEN_EXPIRED`, `AUTH_TOKEN_INVALID` o `AUTH_TOKEN_MALFORMED`, la app SHALL borrar el token, quedar sin sesión con la marca de expiración, y la petición MUST terminar con un error de sesión expirada.

#### Scenario: Token malformado
- **WHEN** la sesión tiene el token `abc.def` y se pide `GET /users/me`
- **THEN** la petición termina con sesión expirada, no hay sesión y la marca de expiración está activa

#### Scenario: Token vencido
- **WHEN** la sesión tiene un token expirado obtenido con `POST /test/tokens/expired` y se pide `GET /users/me`
- **THEN** la petición termina con sesión expirada y la marca de expiración está activa

### Requirement: Token ausente
Si una ruta protegida responde 401 con `code` `AUTH_TOKEN_MISSING`, la app SHALL quedar sin sesión **sin** la marca de expiración, porque significa que la app no envió el token (un bug), y la petición MUST terminar con un error de no autenticado.

#### Scenario: Ruta protegida sin token
- **WHEN** no hay sesión y se pide `GET /users/me`
- **THEN** la petición termina con no autenticado y la marca de expiración no está activa
