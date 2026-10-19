# Tareas: bootstrap

Verificación: `./gradlew :app:testDebugUnitTest` (los de integración requieren el backend levantado), `./gradlew :app:assembleDebug :app:assembleRelease`.

## 1. Proyecto y tema

- [x] 1.1 Proyecto Gradle con catálogo de versiones, wrapper 9.4.1, variantes debug/release y `BuildConfig.API_BASE_URL`.
- [x] 1.2 Manifiesto (`allowBackup=false`, reglas de extracción), red de debug para `10.0.2.2`/`localhost`/`.local`.
- [x] 1.3 Tema claro/oscuro con colores semánticos; verificado en emulador.

## 2. Núcleo

- [x] 2.1 `AppError` y `ErrorMessages` (test primero: `ErrorMessagesTest`).
- [x] 2.2 `ApiClient`, `AppJson`, `BigDecimalSerializer` y `Money` (`JsonAndMoneyTest`, `ApiClientIntegrationTest`).
- [x] 2.3 `SessionManager` con 401 (cubierto por `ApiClientIntegrationTest`).
- [x] 2.4 `KeyValueStore`, `InMemoryStore`, almacén cifrado y `AndroidKeystoreAead` (`EncryptedKeyValueStoreTest`, test instrumentado).
- [x] 2.5 `TestTags` y `AlertText` con el catálogo completo (`TestTagsTest`).

## 3. Estructura mínima

- [x] 3.1 `AppContainer`, `ShopApp`, pestañas provisionales y `AccountScreen` con el selector de tema (`SettingsRepositoryTest`).
- [x] 3.2 `./gradlew :app:testDebugUnitTest` pasa (35 tests) y el instrumentado pasa en emulador; debug y release compilan.
