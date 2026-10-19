# Propuesta: bootstrap

## Why

La app Android necesita una base compilable antes de cualquier pantalla: capas, comunicación con el backend, errores, sesión, tema claro/oscuro y almacenamiento local, con versiones de Kotlin, Compose, Ktor y AGP verificadas entre sí. Corresponde a la iteración A1 de `docs/plan-iteraciones-android.md` y se registra en OpenSpec en la A0, con el trabajo ya hecho y verificado.

## What Changes

- **Proyecto:** Kotlin 2.4 + Compose (BOM) + Material 3, AGP 9.2 con Gradle 9.4, minSdk 26, compile/target 37; variantes debug y release.
- **Tema:** claro y oscuro con colores semánticos propios (sin Material You dinámico).
- **Red:** `ApiClient` con Ktor (OkHttp, kotlinx.serialization), timeout de 15 s, errores del backend convertidos en `AppError`, `CancellationException` relanzada.
- **Sesión:** `SessionManager` en memoria con fin normal y expiración por 401.
- **Mensajes:** `ErrorMessages` por `code` del backend.
- **Almacenamiento:** `KeyValueStore` con `InMemoryStore` y almacén cifrado con Tink + Android Keystore.
- **Contrato con el catálogo:** `TestTags` con todos los IDs del catálogo y `AlertText`.
- **Estructura:** `AppContainer` (DI manual), tres pestañas provisionales y `AccountScreen` con el selector de tema.
- **Tests:** unitarios JVM, integración contra el backend real y un test instrumentado de Keystore.

## Capabilities

### New Capabilities

- `app-platform`: versiones soportadas, tema claro/oscuro, red de desarrollo, copias de seguridad y almacenamiento cifrado.
- `backend-communication`: base URL, cabeceras, timeout, errores del backend y montos exactos.
- `session-management`: sesión en memoria y reacción a los 401.
- `user-error-messages`: texto para el usuario por `code`.

### Modified Capabilities

Ninguna.

## Impact

- Proyecto nuevo `ecommerce_shop_android`; sin cambios en el backend ni en iOS.
- Los tests de integración necesitan `docker compose up` en `../ecommerce_shop_api`.
- Las fechas del backend se tratan en las iteraciones que las muestran (órdenes).
