# Diseño: bootstrap

Decisiones tomadas (detalle y razones en `docs/shop-android-arquitectura.md`, §14):

- **Versiones:** AGP 9.2.1 porque Android Studio Panda 4 admite hasta AGP 9.2; con AGP 9 Kotlin viene integrado (sin `org.jetbrains.kotlin.android`). El BOM de Compose fija Compose y Material 3. La tabla de compatibilidad está en el README.
- **Red:** Ktor con OkHttp. `ApiClient.get/post` son `inline reified`; `execute` y `decode` son `@PublishedApi`. Los 401 con tokens `EXPIRED/INVALID/MALFORMED` terminan la sesión con aviso; `MISSING` la termina sin aviso.
- **Montos:** `BigDecimal` con `BigDecimalSerializer`; nunca `Double`.
- **Almacenamiento:** se mantiene `KeyValueStore` sobre SharedPreferences (no Room/SQLite: son pocos datos clave-valor). Cifrado con Tink AEAD + Keystore porque `EncryptedSharedPreferences` está obsoleta desde `security-crypto` 1.1.0-alpha07.
- **Estado:** `ViewModel` con `StateFlow<UiState>`; DI manual con `AppContainer`.
- **IDs:** `testTag` con `TestTags`; `testTagsAsResourceId` una sola vez en la raíz.
- **Tests:** JVM contra el backend real (`localhost:8080`), una sola JVM de test (`maxParallelForks = 1`); Keystore en un test instrumentado.
- **Pendiente para después:** extra de arranque para cambiar la base URL y hooks de prueba (A9), reglas de R8 y ejecución del APK de release (A9), fechas del backend (A8).
