# Tareas: mobile-testing

Verificación (desde la raíz del repositorio):

- **UNIT:** `./gradlew :app:testDebugUnitTest --tests '<Clase>'` (sin red).
- **ALL:** `./gradlew :app:testDebugUnitTest`. **Requiere el backend levantado** (`docker compose up` en `../ecommerce_shop_api`, perfil `testing`; `curl http://localhost:8080/actuator/health` responde `UP`).
- **BUILD:** `./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug`.
- **IDs:** `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml`, o `maestro hierarchy`.

Antes de empezar: `account-orders` está commiteado y archivado.

## 1. IDs en las dos direcciones (test primero)

- [x] 1.1 Ampliar `TestTagsTest` (falla): todo ID de la primera columna de las tablas del catálogo existe en `TestTags` o en la lista de excepciones; las plantillas con `<…>` se comprueban contra las funciones dinámicas (design.md §1). Verificar: UNIT falla con la lista real de IDs que faltan.
- [x] 1.2 Resolver cada ID que falte: añadirlo a la app o, si es solo de iOS, dejarlo en la lista de excepciones con su motivo. Verificar: UNIT pasa.
- [x] 1.3 Con `uiautomator dump`: llegan `tab_catalog`, `tab_cart` y `tab_account`, la casilla de registro, el desplegable de país y los botones de los diálogos. Verificar: lista de IDs encontrados; lo que no llegue se anota.

## 2. R8 y release

- [x] 2.1 Reglas de `kotlinx.serialization`, Ktor y OkHttp en `proguard-rules.pro`, cada una con comentario (design.md §3). Verificar: BUILD pasa.
- [x] 2.2 Instalar y **ejecutar** el APK de release en el emulador con la URL local: login, catálogo, carrito, compra y "My orders"; `adb logcat` sin `ClassNotFoundException` ni `SerializationException`. Cada fallo se corrige con una regla; la configuración temporal se revierte. Verificar: recorrido descrito en el informe.

## 3. Maestro en Android

- [x] 3.1 Copiar `maestro/` de iOS a `maestro/` de este repositorio con `appId: co.com.learningswift.shop`, y `run.sh` con el `pm grant` del permiso de red local y el `reset` (design.md §2). Verificar: `./maestro/run.sh emulator-5554 flows/00-humo.yaml` pasa.
- [x] 3.2 `01-login` y `03-catalogo-y-carrito-lleno`: ajustar solo lo que falle. Verificar: pasan.
- [x] 3.3 `02-registro` y `06-checkout-exito`: ajustar teclado, casilla y desplegable. Verificar: pasan.
- [x] 3.4 Repetir los cinco en tema claro y oscuro (`adb shell cmd uimode night yes|no`). Verificar: pasan en ambos.

## 4. Cierre

- [x] 4.1 Ejecutar ALL y BUILD completos; revisar `lint` y el README. Verificar: todo pasa.
- [x] 4.2 Crear `docs/guia-pruebas-maestro-android.md` (cómo ejecutar, ajustes frente a iOS), actualizar `docs/plan-iteraciones-android.md` (A9 parcial y lo diferido) y `docs/shop-android-arquitectura.md` §10.4. Verificar: `openspec validate mobile-testing --strict` pasa.

## Resultados

- **IDs:** `TestTagsTest` ahora comprueba las dos direcciones (IDs de la primera columna, botones de alertas y plantillas) y el catálogo y la app coinciden sin excepciones. Defecto encontrado y corregido: el test leía el catálogo sin que Gradle lo supiera como entrada, y reutilizaba un resultado viejo (`FROM-CACHE`); ahora está declarado en `build.gradle.kts`.
- **R8:** reglas de `kotlinx.serialization` y de los DTO; Ktor y OkHttp no necesitaron reglas. El APK minificado (no depurable) pasó el flujo `06` completo y `logcat` sin errores de clases ni de serialización. La configuración temporal `releaseLocal` se borró.
- **Maestro:** `00`, `01`, `02`, `03` y `06` pasan en el emulador (Android 17). Ajustes: `hideKeyboard` en `02` y `06` y `back` en `06`. El `06` también en oscuro, por decisión del responsable, y contra el release.
- **No se hizo (recortado):** extras de arranque, emulador API 34, flujos adicionales y repetir todos los flujos en oscuro.
- **Pruebas:** 245 en JVM, 0 fallos; `assembleDebug`, `assembleRelease` y `lintDebug` limpios.
