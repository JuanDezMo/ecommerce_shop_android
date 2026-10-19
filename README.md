# ShopLab · App Android

Proyecto base de la app Android de ShopLab: **Kotlin + Jetpack Compose + Material 3**, arquitectura **MVVM**, **Ktor** para el backend, modo **claro y oscuro**. Sigue la [propuesta de arquitectura](docs/shop-android-arquitectura.md) y es la contraparte de la app iOS.

| Documento | Para qué |
| --- | --- |
| [Arquitectura Android (propuesta)](docs/shop-android-arquitectura.md) | Capas, SOLID, código de referencia, plan de iteraciones |
| [Plan de iteraciones](docs/plan-iteraciones-android.md) | Qué se construye en cada iteración, con archivos, tests y criterios |
| [Catálogo de IDs](docs/catalogo-ids-accesibilidad.md) | Copia del catálogo compartido con iOS (una prueba la compara con `TestTags`) |
| [Comportamiento de las apps](docs/comportamiento-apps-moviles.md) | Qué hace cada pantalla |

## Qué trae este proyecto base

- **Estructura** por capas y funcionalidades (`core`, `data`, `features`, `app`).
- **MVVM de ejemplo** en la pestaña Account: `AccountUiState` + `AccountViewModel` (StateFlow) + `AccountScreen`/`AccountContent`.
- **Red con Ktor** (`ApiClient`): errores traducidos a `AppError`, 401 gestionado en un solo lugar, montos con `BigDecimal`.
- **Sesión** (`SessionManager`), **errores** (`AppError`, `ErrorMessages`, `AlertText`) y **tema** (`ShopTheme`: System, Light o Dark; el elegido se guarda).
- **Pestañas** Catalog, Cart y Account (Catalog y Cart son provisionales hasta sus iteraciones) con los IDs del catálogo y `testTagsAsResourceId`.
- **Datos sensibles cifrados** (`EncryptedKeyValueStore`, Tink + Android Keystore), en lugar de la `EncryptedSharedPreferences` obsoleta. Hoy solo está disponible: el token sigue en memoria.
- **35 pruebas en la JVM** (unitarias y de integración contra el backend real) y **2 en el emulador** (Keystore real).

## Cómo ejecutarlo

Requisitos: JDK 21 (sirve cualquier JDK 17+), Android SDK (compileSdk 37) y, para las pruebas de integración y la app, el backend local (`docker compose up` en `../ecommerce_shop_api`).

```bash
./gradlew :app:testDebugUnitTest      # pruebas (las de integración necesitan el backend)
./gradlew :app:assembleDebug          # compila el APK de debug
./gradlew :app:lintDebug              # revisión estática
./gradlew :app:connectedDebugAndroidTest   # pruebas del Keystore (necesita un emulador o dispositivo)
```

Abrir el proyecto en **Android Studio** (carpeta raíz). La app de debug usa `http://10.0.2.2:8080/api/v1/` (el backend de la Mac visto desde el emulador); el HTTP sin TLS solo se permite en `debug` y para hosts concretos (`app/src/debug/res/xml/network_security_config.xml`).

**Android 17 (API 37) y la red local:** para hablar con `10.0.2.2` (o el `.local` de la Mac) la app necesita el permiso `ACCESS_LOCAL_NETWORK`; sin él, las peticiones terminan por tiempo agotado y la app muestra "Check your internet connection". Está declarado **solo en `debug`** (`app/src/debug/AndroidManifest.xml`) y `MainActivity` lo pide al abrir; si se rechaza: `adb shell pm grant co.com.learningswift.shop android.permission.ACCESS_LOCAL_NETWORK`. Las suites de Maestro deben concederlo antes de empezar (A9).

## Compatibilidad de versiones (comprobada el 2026-10-19)

| Pieza | Versión | Por qué esta |
| --- | --- | --- |
| Android Studio instalado | Panda 4 (2025.3.4) | Admite AGP **hasta 9.2** |
| Android Gradle Plugin | **9.2.1** | La más nueva que admite el Studio instalado (la 9.4.1 existe, pero Studio la marcaría incompatible) |
| Gradle | **9.4.1** | Mínimo y por defecto de AGP 9.2 |
| JDK | 21 (mínimo 17) | AGP 9.2 pide 17 o más; 21 es el del backend |
| Kotlin | **2.4.21** | AGP 9.2 pide KGP 2.2.10 o más. **AGP 9 trae Kotlin integrado: no se aplica `org.jetbrains.kotlin.android`** |
| Plugin de Compose y de serialización | 2.4.21 | Siempre **la misma versión que Kotlin** |
| Compose BOM | 2026.09.00 | Fija Compose y Material 3 a la vez: no se declaran sus versiones sueltas |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 | AGP 9.2 admite hasta la API 37; la 26 trae `java.time` de serie |
| Activity Compose / Navigation Compose / Lifecycle | 1.13.0 / 2.10.2 / 2.11.0 | Últimas estables |
| Coroutines / kotlinx.serialization | 1.11.0 / 1.11.0 | Últimas estables |
| Ktor | 3.6.0 | Última estable; motor OkHttp |
| Tink (`tink-android`) | 1.24.0 | Cifrado AEAD; reemplaza a `EncryptedSharedPreferences` (obsoleta). `datastore-tink` aún no tiene versión estable |

Resultado: compila (debug y release con R8), pasa `lint` y todas las pruebas, y la app se ejecutó en un emulador API 37 en claro y oscuro.

**Para subir versiones:** primero la que limita (Android Studio → AGP → Gradle → Kotlin → el resto) y, tras cada cambio, `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease`.

## Pendiente conocido

- **R8 en release:** el build `assembleRelease` compila, pero no se ha ejecutado el APK minificado; faltan las reglas de `kotlinx.serialization` y Ktor (`app/proguard-rules.pro`) antes de publicar.
- **Lint:** 5 avisos informativos (versión de AGP más nueva disponible, `UseKtx`, `NotShrinkingResources`, `ObsoleteSdkInt`).
- **Login y registro, imágenes de producto, catálogo y demás funcionalidades:** llegan en sus iteraciones (plan en [`docs/plan-iteraciones-android.md`](docs/plan-iteraciones-android.md)).
