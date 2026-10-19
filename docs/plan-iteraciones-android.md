# Plan de iteraciones · App Android ShopLab

Actualizado: 2026-10-19 · **Estado: aprobado. A0, A2, A3, A4, A5 y A6 hechas; siguiente: A7.**

Plan de implementación de la app Android, iteración por iteración, para que cualquier desarrollador (también de perfil junior) pueda ejecutarlo sin conocer las conversaciones previas. Es el equivalente del [plan de iOS](../../ecommerce_shop_ios/docs/plan-iteraciones-ios.md), **recortado a lo que Android realmente necesita**: el comportamiento, los IDs, los tests y los flujos de Maestro ya existen y se reutilizan.

| Documento | Qué consultar |
| --- | --- |
| [Arquitectura Android](shop-android-arquitectura.md) | **Cómo** se construye: capas, SOLID, código de referencia |
| [Comportamiento de las apps](comportamiento-apps-moviles.md) | **Qué** hace cada pantalla (común a iOS y Android). Manda sobre todo lo demás |
| [Catálogo de IDs](catalogo-ids-accesibilidad.md) | IDs y textos de alertas (copia en este repositorio) |
| [Guía de integración](guia-integracion-ios-android.md) | Contrato del backend |
| [Guía de pruebas con Maestro](../../ecommerce_shop_ios/docs/guia-pruebas-maestro-ios.md) y carpeta `maestro/` de iOS | Flujos que se reutilizan |
| [Plan de iOS](../../ecommerce_shop_ios/docs/plan-iteraciones-ios.md) y sus specs en `../ecommerce_shop_ios/openspec/specs/` | La referencia funcional ya validada |

En este plan, "Comportamiento §N" remite al documento de comportamiento y "Arquitectura §N" al de arquitectura Android.

---

## 1. Antes de empezar

### 1.1 Entorno

| Necesidad | Cómo |
| --- | --- |
| Android Studio | Panda 4 (2025.3.4) o más nuevo; si se actualiza, revisar la tabla de compatibilidad del [README](../README.md) |
| JDK | 21 (mínimo 17). En Android Studio: *Settings → Build → Gradle → Gradle JDK* |
| Android SDK | Plataforma 37 y *Build Tools* 36 o más |
| Backend | En `../ecommerce_shop_api`: `docker compose up` (con Colima si es necesario). Comprobar `curl http://localhost:8080/actuator/health` → `{"status":"UP"}` |
| Emulador | Uno con API 34 o más y otro actual (hoy: Pixel_8 con API 37). Para pruebas de interfaz, **apagar el autorrelleno de contraseñas** del emulador |
| Maestro | Ya instalado (`maestro --version`); se usa en la iteración A9 |

### 1.2 Flujo de cada iteración

Cada iteración es un *change* de OpenSpec:

1. `/opsx:propose <nombre>`. **Atajo (ver §3):** los `specs` se copian de iOS y solo se escriben `design.md` y `tasks.md`.
2. Revisión de la propuesta por el responsable. **No se implementa nada antes de su aprobación.**
3. `/opsx:apply`: tests primero (TDD) donde haya lógica.
4. El responsable revisa en el emulador y hace el commit.
5. `/opsx:archive`.

### 1.3 Convenciones

- **Idioma:** código en inglés; comentarios y documentación en español; textos de la UI en inglés.
- **Capas:** `Screen → ViewModel → Repository → ApiClient` (Arquitectura §3). Las pantallas nunca llaman a repositorios; los ViewModels nunca ven DTOs ni `Context`.
- **Textos e IDs:** siempre desde `AlertText`, `ErrorMessages` y `TestTags`, nunca sueltos en los composables. **Un ID nuevo se añade primero al catálogo y luego a `TestTags`** (una prueba lo comprueba).
- **Colores:** solo los del tema (`MaterialTheme.colorScheme`), nunca un color fijo.
- **Interfaces:** solo con dos implementaciones reales hoy (la única es `KeyValueStore`).
- **`catch`:** se atrapa `AppError`, nunca `Exception` a secas (se tragaría la cancelación de una corrutina).
- **Dinero:** `BigDecimal`, nunca `Double`.
- **Sin mocks:** unitarios solo para lógica pura; ViewModels y repositorios se prueban contra el backend real.

### 1.4 Cómo ejecutar los tests

```bash
./gradlew :app:testDebugUnitTest              # JVM: unitarios + integración (necesita el backend)
./gradlew :app:connectedDebugAndroidTest      # emulador: solo lo que exige Android (Keystore)
./gradlew :app:assembleDebug :app:lintDebug :app:assembleRelease
```

### 1.5 Definición de terminado (todas las iteraciones)

- [ ] Compila (debug y release) sin errores ni advertencias nuevas, y `lint` sin errores.
- [ ] Todos los tests pasan, los unitarios y los de integración.
- [ ] Las pantallas nuevas funcionan en **modo claro y oscuro**, en dos emuladores, con el teclado abierto y con texto grande.
- [ ] Cada ID y cada texto de alerta coincide con el catálogo.
- [ ] El comportamiento coincide con el documento de comportamiento.

---

## 2. Resumen

Se pasa de 11 iteraciones en iOS a **9 en Android**, porque lo que en iOS fueron decisiones y descubrimientos (origen de las imágenes, forma de la raíz, hooks de prueba) ya está resuelto.

| # | Iteración | Change de OpenSpec | Tamaño | Entregable |
| --- | --- | --- | --- | --- |
| A0 | Preparación | `bootstrap` (archivado) | **HECHO** | OpenSpec en el repositorio, documentos copiados, `config.yaml` |
| A1 | Base del proyecto | `bootstrap` | **HECHO** | Gradle, tema claro/oscuro, red con Ktor, errores, sesión, cifrado, pestañas |
| A2 | Estructura de la app | `app-shell` | S | Raíz por sesión, snackbar |
| A3 | Login | `user-login` | M | Pantalla de login y sesión |
| A4 | Registro | `user-registration` | L | Formulario en dos pantallas con validaciones |
| A5 | Catálogo | `product-catalog` | L | Imágenes, lista, detalle, hoja "Add to cart" y reglas del carrito |
| A6 | Carrito | `cart` | M | Pantalla del carrito y refresco del stock |
| A7 | Checkout | `checkout` | L | Formulario, compra y confirmación |
| A8 | Cuenta y órdenes | `account-orders` | M | Perfil, tema, logout, My orders y detalle |
| A9 | Pruebas móviles | `mobile-testing` | M | Hooks de prueba, flujos de Maestro y revisión de IDs |

Tamaños relativos: S = unas pocas horas, M = una jornada, L = una a dos jornadas (con revisión del responsable en cada una).

**Qué se fusionó respecto a iOS:** `product-images` entra en `product-catalog` (las imágenes ya están decididas: los 7 SVG de iOS), `account` y `orders` van juntas (comparten repositorio y navegación), y `bootstrap` y `app-shell` están casi hechos.

Dependencias: A2 antes de A3; A5 antes de A6; A6 antes de A7. A8 puede ir después de A3 y A5.

---

## 3. Cómo se hace más rápido que en iOS

| Palanca | En la práctica |
| --- | --- |
| **Los `specs` ya están escritos** | Cada capacidad de Android es igual a una de iOS (`user-login`, `user-registration`, `form-validation`, `product-catalog`, `add-to-cart`, `cart-screen`, `checkout`, `order-confirmation`, `user-account`, `app-appearance`, `order-history`, `order-detail`…). En `openspec/changes/<nombre>/specs/` se **copian de `../ecommerce_shop_ios/openspec/specs/<capacidad>/spec.md`** y solo se ajustan los términos de plataforma (§4). Son contrato de comportamiento: no hablan de Swift |
| **Los tests ya están listados** | Las secciones "Tests" de cada iteración de iOS son la lista de casos de Android; se traducen a JUnit con los mismos datos (`standard`, `buyer`, `Secret_123`…) |
| **Los IDs ya existen** | `TestTags` se completa copiando del catálogo; no hay que decidir nombres |
| **Las imágenes ya existen** | Los 7 SVG de `docs/imagenes-producto/` se importan como vectores |
| **Los flujos de Maestro ya funcionan** | La carpeta `../ecommerce_shop_ios/maestro/` se reutiliza con `appId` como variable (A9); solo cambian el teclado y el selector de cantidad |
| **El código de referencia está escrito** | Arquitectura §5 tiene `ApiClient`, ViewModel, pantalla y carrito; el proyecto base ya los trae |
| **Cada iteración trae su lista de archivos** | §6: qué crear y dónde, para que el trabajo sea mecánico |
| **Las trampas ya se conocen** | Cada iteración lista los problemas que costaron tiempo en iOS |

**Qué NO se acelera** (y por qué): la revisión del responsable en el emulador tras cada iteración y el commit propio. Se mantienen porque fueron lo que evitó retrabajo en iOS.

---

## 4. Equivalencias iOS → Android

| Concepto en iOS | En Android (Compose) |
| --- | --- |
| `@Observable` ViewModel | `ViewModel` + `StateFlow<UiState>` |
| `NavigationStack(path:)` | `NavHost` (una pila por pestaña) |
| `.alert(...)` | `AlertDialog` (ventana aparte: repetir `testTagsAsResourceId` dentro) |
| `.confirmationDialog(...)` (hoja de acciones) | `AlertDialog` (el catálogo ya lo prevé así) |
| `.sheet` con `presentationDetents` | `ModalBottomSheet` |
| `List` + `.refreshable` | `LazyColumn` + `PullToRefreshBox` |
| `.swipeActions` ("Delete") | `SwipeToDismissBox` |
| `Stepper` | Dos botones con ID propio: `<id>-Increment` y `<id>-Decrement` (igual que expone iOS) |
| `Picker(.menu)` | `ExposedDropdownMenuBox` |
| `Picker(.segmented)` | `SingleChoiceSegmentedButtonRow` |
| `Toggle` | `Switch` dentro de una fila clicable |
| Casilla de términos (botón con `checked`/`unchecked`) | `Checkbox` con `semantics { stateDescription = "checked" / "unchecked" }` |
| `.badge(n)` de la pestaña | `BadgedBox` |
| `@FocusState` + `ScrollViewReader` | `onFocusChanged` + `BringIntoViewRequester` / `LazyListState.animateScrollToItem` |
| `UserDefaults` | `SharedPreferences` tras `KeyValueStore` |
| `JSONCoding` (fechas con decimales) | `Instant.parse` ya los acepta; `BigDecimalSerializer` para montos |
| Esquema de pruebas `IntegrationTests` serializada | `maxParallelForks = 1` y un `reset` en cada `@BeforeTest` |
| Argumentos de arranque (`-testForceError`) | *Extras* del `Intent` solo en `debug` |
| Teclado: `.scrollDismissesKeyboard` | `imePadding()` + `verticalScroll` |

---

## 5. Estado de partida (lo que ya está hecho)

El proyecto base (`/Users/juandezmo/Documents/claude_projects/ecommerce_shop_android`) ya trae, compilado y probado:

| Área | Qué hay |
| --- | --- |
| Build | AGP 9.2.1, Gradle 9.4.1, Kotlin 2.4.21, Compose BOM 2026.09.00, Ktor 3.6.0, Tink 1.24.0 |
| Red | `ApiClient` (Ktor), `ErrorDto`, `AppConfig`, `AppJson` y `BigDecimalSerializer` |
| Errores | `AppError`, `ErrorMessages` (tabla por `code`), `AlertText` (solo la alerta genérica y los snackbars) |
| Sesión | `SessionManager` (token en memoria) |
| Tema | `ShopTheme` claro/oscuro/sistema, `SettingsRepository` y `ThemeMode`, selector en Account |
| Datos | `Product`, `ProductDto`, `ProductRepository`, `KeyValueStore`, `EncryptedKeyValueStore` |
| UI | `ShopApp`, tres pestañas con sus IDs, `GenericErrorDialog`, `LoadingOverlay`, `MessageBanner`, `PlaceholderScreen` |
| Pruebas | 35 en la JVM (8 de integración con el backend real) y 2 en el emulador (Keystore) |

---

## Iteración A0 · Preparación

**Objetivo:** dejar el repositorio listo para trabajar con OpenSpec y con los mismos documentos que iOS.

**Tamaño:** S.

### Actividades

- [x] `openspec init` en `ecommerce_shop_android` y crear `openspec/config.yaml` adaptado de `../ecommerce_shop_ios/openspec/config.yaml`: contexto en español, stack Kotlin/Compose/Ktor, capas, "sin mocks", "interfaz solo con dos implementaciones" y las reglas de tareas (test primero, cómo verificar con `./gradlew`).
- [x] Copiar a `docs/` los documentos compartidos que no estén: `comportamiento-apps-moviles.md`, `guia-integracion-ios-android.md` y la carpeta `imagenes-producto/`. **Regla:** un cambio de comportamiento se hace en un solo lugar y se copia al otro repositorio el mismo día.
- [x] Mover `shop-android-arquitectura.md` a este repositorio (`docs/`) y actualizar los enlaces.
- [x] Marcar en Arquitectura §13 que el plan es este documento.
- [x] Registrar el proyecto base como `bootstrap` en OpenSpec y archivarlo (el commit lo hace el responsable) en OpenSpec (los specs de `bootstrap` de iOS sirven de base: `backend-communication`, `user-error-messages`, `session-management`, `app-platform`).

### Criterios de aceptación

- [x] `openspec list` funciona y `./gradlew :app:testDebugUnitTest` sigue en verde.

---

## Iteración A2 · `app-shell`

**Objetivo:** la raíz de la app decide entre el flujo de autenticación y las pestañas según la sesión, y existe el snackbar.

**Referencias:** Arquitectura §3.3 y §7.2; specs de iOS `app-navigation`, `app-feedback`.

**Tamaño:** S.

### Archivos

| Archivo | Qué |
| --- | --- |
| `core/design/SnackbarCenter.kt` | Mensaje temporal (3 s); uno nuevo reemplaza al actual y reinicia el tiempo |
| `core/design/ShopSnackbarHost.kt` | Lo dibuja con el ID `app_snackbar_text` |
| `app/ShopApp.kt` | Si `session.isLoggedIn` → pestañas; si no → `AuthHost` provisional (un texto "Log in") |
| `app/AppContainer.kt` | Añadir `snackbar` |

### Tests

- [x] `SnackbarCenterTest` con `runTest` y tiempo virtual: aparece, desaparece a los 3 s, y un mensaje nuevo reinicia el tiempo (como `SnackbarCenterTests` de iOS).
- [x] `session.start()` / `end()` / `expire()` cambian lo que muestra la raíz (prueba de composición o de estado).

### Criterios de aceptación

- [x] Iniciar y cerrar sesión (desde un botón provisional de depuración) cambia entre login y pestañas.
- [x] Un snackbar de prueba aparece sobre la barra de pestañas y se ve en claro y oscuro.

---

## Iteración A3 · `user-login`

**Objetivo:** login funcional contra el backend real, con sus errores.

**Referencias:** Comportamiento §2 y §4; Arquitectura §5.2 a §5.8; specs de iOS `user-login`, `session-management`.

**Tamaño:** M.

### Archivos

| Archivo | Qué |
| --- | --- |
| `data/remote/dto/LoginRequestDto.kt`, `LoginResponseDto.kt` | `@Serializable` (`accessToken`) |
| `data/repository/AuthRepository.kt` | `login(email, password)`: recorta el email, llama `POST auth/login` y hace `session.start(token)` |
| `core/testing/ForcedError.kt` | Solo `debug`: un email `force<400–599>@…` añade `X-Force-Error` |
| `features/login/LoginUiState.kt`, `LoginViewModel.kt`, `LoginScreen.kt` | El patrón de Arquitectura §5.7 y §5.8 |
| `core/testing/TestTags.kt` | `Login.*` completo |
| `app/AppContainer.kt` | `authRepository` |

### Actividades

- [x] `LoginViewModel`: errores en línea solo para `INVALID_CREDENTIALS` y `USER_LOCKED`; cualquier otro, alerta genérica con "Try again" → `login()`. Vaciar el email borra el mensaje.
- [x] `LoginScreen`: aviso de sesión expirada (`login_session_expired_text`), botón "Log in" (deshabilitado con campos vacíos) y "Create account".

### Tests

**Integración (`LoginIntegrationTest`, con el backend real)**

- [x] `standard@shoplab.dev` / `Secret_123` → sesión iniciada; contraseña errónea → "The email or password is incorrect."; `locked@shoplab.dev` → mensaje de cuenta bloqueada.
- [x] Email con espacios y mayúsculas se normaliza.
- [x] `force503@shoplab.dev` → `genericError` con el mensaje del servidor.
- [x] Token expirado (`POST /api/v1/test/tokens/expired`) → `session.expire()` y la raíz muestra el aviso.

### Trampas conocidas

- **Android 17 y la red local:** sin el permiso `ACCESS_LOCAL_NETWORK` (solo debug) el login se agota. Ver README.
- El botón debe poder pulsarse con el teclado abierto: `imePadding()` y `verticalScroll`.
- Aplicar `Modifier.testTag` al `OutlinedTextField`, no a un contenedor.

### Criterios de aceptación

- [x] Login correcto lleva a las pestañas; errores y alerta genérica funcionan en el emulador, en claro y oscuro.

---

## Iteración A4 · `user-registration`

**Objetivo:** registro en dos pantallas, con las mismas reglas y mensajes que iOS.

**Referencias:** Comportamiento §5 y §11; Arquitectura §8; specs de iOS `user-registration`, `form-validation`.

**Tamaño:** L.

### Archivos

| Archivo | Qué |
| --- | --- |
| `data/model/Country.kt`, `Plan.kt`, `User.kt` | Seis países americanos (sin `ES`, aunque el backend lo acepte) |
| `data/remote/dto/RegisterRequestDto.kt`, `UserDto.kt` | Teléfono **sin `+`**, email en minúsculas, fecha como texto `yyyy-MM-dd` |
| `data/repository/UserRepository.kt` | `register(form)` |
| `core/errors/FieldErrorMapper.kt` | Genérico: `fieldErrors` del backend → campos de un formulario |
| `features/register/RegisterField.kt`, `RegisterForm.kt`, `RegisterValidator.kt` | Funciones puras |
| `features/register/RegisterViewModel.kt`, `RegisterUiState.kt` | **Un solo ViewModel para las dos pantallas** |
| `features/register/RegisterBasicScreen.kt`, `RegisterDetailsScreen.kt`, `BirthDateField.kt` | Paso 1, paso 2 y los tres desplegables de fecha |
| `core/design/FieldErrorText.kt`, `ErrorSummary.kt`, `LabeledCheckbox.kt` | Piezas comunes (las reutiliza el checkout) |

### Actividades

- [x] `RegisterValidator` con las **reglas de iOS ya endurecidas** (Comportamiento §5.1): nombres y apellidos de 10 a 30 letras A-Z/a-z con un solo espacio entre palabras (se recortan los extremos); email de 8 a 20 caracteres con solo letras, números, `@` y puntos del dominio; teléfono obligatorio `+` más 3 dígitos de prefijo y 10 dígitos; contraseña de 10 a 20 caracteres ASCII con mayúscula, minúscula, número y especial; edad con año, mes y día de la fecha elegida contra hoy **en UTC**; país y términos obligatorios.
- [x] Patrón de formulario (Arquitectura §8.2): botón siempre habilitado, resumen "N fields need attention", desplazamiento al primer error, revalidación al perder el foco, el error desaparece al editar.
- [x] Compartir el ViewModel entre las dos pantallas: con Navigation Compose, **crearlo con el `NavBackStackEntry` del grafo padre** (`navController.getBackStackEntry("register_graph")`); así sobrevive a la flecha atrás.
- [x] Errores del backend: `EMAIL_ALREADY_EXISTS` → email (y vuelve al paso 1), `UNDERAGE_USER` → fecha, `TERMS_NOT_ACCEPTED` → términos, `VALIDATION_ERROR` → `FieldErrorMapper` como red de seguridad.
- [x] Alerta "Account created" con "Log in", que vuelve al login con el email escrito.

### Tests

**Unitarios** (traducir `RegisterValidatorTests`, `RegisterFieldTests`, `FieldErrorMapperTests`, `RegisterRequestDTOTests` y `BirthDateOptionsTests` de iOS con sus mismos datos): límites de 10 y 30 letras, un solo espacio, correo con `_`, teléfono con y sin `+`, 29 de febrero, mayoría de edad por UTC, `+` quitado al enviar.

**Integración (`RegistrationIntegrationTest`):** registro válido → 201; `standard@shoplab.dev` → `EMAIL_ALREADY_EXISTS`; menor de edad; términos sin aceptar; `force503@…` → alerta genérica.

### Trampas conocidas

- El día de la fecha baja al último del mes al cambiar mes o año (`BirthDateOptions`).
- **Autorrelleno de contraseñas** del emulador: apagarlo para las pruebas de interfaz.
- El teclado numérico del teléfono no tiene tecla de retorno y tapa el botón "Next": se usa `ImeAction.Done` (su ✓ cierra el teclado) y `imePadding` + scroll.
- El teclado del emulador **autocorrige** nombres y correos: `autoCorrectEnabled = false` en los campos de texto.
- Con el teclado abierto los campos de abajo quedan tapados: en las pruebas de interfaz cerrar el teclado entre campos.
- La casilla se ve en `uiautomator` como `checkable`/`checked` (no se ve `stateDescription`): en Maestro usar `checked: true`.

### Criterios de aceptación

- [x] Se crea una cuenta contra el backend real, con todos los errores en claro y oscuro y con el teclado abierto.

---

## Iteración A5 · `product-catalog`

**Objetivo:** ver los productos, su detalle y añadirlos al carrito, con las imágenes y las reglas del carrito.

**Referencias:** Comportamiento §6 y §7.1; Arquitectura §6 y §9; specs de iOS `product-image-display`, `product-catalog`, `add-to-cart`.

**Tamaño:** L.

### Archivos

| Archivo | Qué |
| --- | --- |
| `res/drawable/product_<clave>.xml` (×6) y `product_placeholder.xml` | Los 7 SVG importados (*Resource Manager → Vector Asset*), revisados en claro y oscuro |
| `core/design/ProductImage.kt`, `ProductImageView.kt` | Mapeo único `imageKey` → `@DrawableRes`; imagen cuadrada con ID `product_image_<imageKey>` y "Sold out" |
| `data/model/CartItem.kt`, `data/repository/CartRepository.kt` | Reglas: una línea por producto, máximo 20 unidades, nunca más que el stock; guardado como JSON |
| `features/productdetail/AddToCartAction.kt`, `AddToCartFlow.kt` | Regla única del botón (`Disabled`, `CartFull` o `OpenSheet`) |
| `features/catalog/CatalogUiState.kt`, `CatalogViewModel.kt`, `CatalogScreen.kt` | Lista con `PullToRefreshBox`, "Sold out" y texto de error tras "Cancel" |
| `features/productdetail/ProductDetailViewModel.kt`, `ProductDetailScreen.kt` | Detalle con stock ("10 in stock") |
| `features/productdetail/AddToCartSheet.kt` | `ModalBottomSheet` con selector de cantidad (botones `-Increment` y `-Decrement`) |
| `app/Routes.kt`, `ShopApp.kt` | Pila del catálogo y la **insignia** del carrito (`BadgedBox`) |

### Tests

**Unitarios:** `ProductImageTest` (las seis claves, `unknown`, vacío, distingue mayúsculas, y que cada nombre tiene su recurso), `CartRepositoryTest` con `InMemoryStore` (misma lista que `CartRepositoryTests` de iOS: línea única, tope de stock, tope de 20, `isFull`, persistencia con el precio intacto), `AddToCartActionTest`.

**Integración (`CatalogIntegrationTest`):** seis productos en orden, precios `59.99` y `25.00`, producto 3 agotado, `product(99)` → `PRODUCT_NOT_FOUND`, errores de carga y de detalle.

### Trampas conocidas

- El botón "Add to cart" dentro de una fila clicable: que tocarlo no abra el detalle.
- `catalog_item_<id>` es un contenedor: **sin** `mergeDescendants`, o los IDs de sus hijos desaparecen.
- Recargar la lista cada vez que la pestaña aparece (como en iOS), no solo la primera vez: `LifecycleEventEffect(ON_RESUME)`.
- **`Modifier.clickable` fusiona la semántica de los hijos** (también `Card(onClick)`): usar `clickableKeepingChildren` (en `core/design`) para que los IDs de nombre, precio y botón lleguen a Maestro.
- Las acciones de los botones se calculan donde se lee el carrito, **fuera** del `LazyColumn`; si no, las filas no se redibujan al cambiar el carrito.
- La insignia del carrito no expone su texto a `uiautomator`: comprobarla a la vista.
- Los vectores salen de `scripts/svg_to_vector.py`; si cambia un SVG, regenerarlos.

### Criterios de aceptación

- [x] El producto 3 sale como "Sold out" con el botón deshabilitado; con 20 unidades aparece "Cart is full"; la insignia muestra el total de unidades; el carrito sobrevive a cerrar y abrir la app.

---

## Iteración A6 · `cart`

**Objetivo:** la pantalla del carrito, con el stock real y la edición de cantidades.

**Referencias:** Comportamiento §7; specs de iOS `cart-screen`.

**Tamaño:** M.

### Archivos

| Archivo | Qué |
| --- | --- |
| `data/repository/CartRepository.kt` | Añadir `setQuantity(quantity, productId, stock)`, `remove`, `clear`, `reconcile(products)` |
| `features/cart/CartUiState.kt`, `CartViewModel.kt`, `CartScreen.kt` | Líneas con imagen, nombre, selector y subtotal; total; "Clear cart" con `AlertDialog`; estado vacío; "Checkout" solo con líneas |
| `features/cart/CartLineRow.kt`, `core/design/SwipeToRevealRow.kt` | Fila con "Delete" que se muestra al deslizar y espera el toque (`SwipeToDismissBox` no sirve: descarta al soltar o vuelve, no deja un botón esperando) |

### Tests

- [x] Unitarios (`CartRepositoryTest`): `reconcile` baja cantidades y quita agotados o inexistentes (y devuelve `false` si no cambió nada); `setQuantity` no supera el stock ni el total de 20; `clear` guarda el carrito vacío.
- [x] Integración (`CartIntegrationTest`): un carrito con el producto 3 → tras `refresh()` la línea desaparece y se muestra el snackbar; con 1 unidad del producto 5 no cambia nada; con el backend caído, alerta genérica y carrito intacto.

### Trampas conocidas

- Sin stock conocido (antes de la primera respuesta) el selector **no deja subir**.
- Para probar el ajuste al stock en el emulador: sembrar `shared_prefs/shop.xml` con `adb shell run-as co.com.learningswift.shop` (app debuggable).
- Un 401 de token no se muestra como error: `ApiClient` ya cerró la sesión.

### Criterios de aceptación

- [x] Deslizar, vaciar con confirmación, editar cantidades y el estado vacío funcionan; el carrito se ajusta al abrir la pestaña.

---

## Iteración A7 · `checkout`

**Objetivo:** comprar con `POST /orders`, resolver cada respuesta y mostrar la confirmación.

**Referencias:** Comportamiento §8, §9, §11.2 y §13; specs de iOS `checkout`, `order-confirmation`.

**Tamaño:** L.

### Archivos

| Archivo | Qué |
| --- | --- |
| `data/model/PaymentMethod.kt`, `Order.kt` | `Card`/`Cash`/`Transfer`; `Order`, `OrderItem`, `Shipping`, con `totalUnits` |
| `data/remote/dto/OrderDto.kt`, `CreateOrderRequestDto.kt` | De cada línea solo `productId` y `quantity` |
| `data/repository/OrderRepository.kt` | `createOrder(items, form)` |
| `features/checkout/CheckoutField.kt`, `CheckoutForm.kt`, `CheckoutValidator.kt` | Dirección 5–120, ciudad 2–60, código postal `^[A-Za-z0-9 -]{3,10}$`, país |
| `features/checkout/CheckoutViewModel.kt`, `CheckoutUiState.kt`, `CheckoutScreen.kt` | Patrón del registro; alertas |
| `features/confirmation/ConfirmationScreen.kt` | Sin botón de atrás (`BackHandler`) |

### Actividades

- [ ] `placeOrderTapped()` valida y abre "Place order?"; `placeOrder()` envía. **El carrito solo se vacía con el 201.**
- [ ] Errores: `INSUFFICIENT_STOCK`, `PRODUCT_NOT_FOUND`, `EMPTY_ORDER`, `DUPLICATE_ORDER_ITEM` → "Can't place your order" con "Review cart"; `VALIDATION_ERROR` → campos (o el mensaje del carrito si no hay campo); otros → alerta genérica.
- [ ] **"Try again" no reenvía:** reabre "Place order?" (el backend no es idempotente).
- [ ] "Done" vacía la pila y cambia a Catalog, que se recarga con el stock.

### Tests

**Unitarios:** `CheckoutValidatorTest` (4, 5, 120 y 121 caracteres; `SW1A 1AA`, `12#45`; solo espacios; país), `FieldErrorMapper` con campos del checkout, `OrderTest` (decodifica la respuesta 201, `totalUnits`).

**Integración (`CheckoutIntegrationTest`, con `standard`):** 2 unidades del producto 1 y 1 del 4 → total `144.98` y carrito vacío; producto 3 → `INSUFFICIENT_STOCK` y carrito conservado; la última unidad del producto 5 comprada dos veces; formulario vacío → 4 errores y ninguna llamada; backend caído → alerta genérica y carrito intacto.

### Trampas conocidas

- Un botón tapado por el teclado: bajar el teclado antes (`LocalSoftwareKeyboardController.hide()`) o usar `imePadding()`.
- El menú de país abre un popup: sus opciones se tocan por texto.

### Criterios de aceptación

- [ ] Carrito → checkout → confirmación → "Done" lleva al catálogo con el stock actualizado.

---

## Iteración A8 · `account-orders`

**Objetivo:** perfil, tema, logout, historial y detalle de órdenes.

**Referencias:** Comportamiento §2, §10 y §14; specs de iOS `user-account`, `app-appearance`, `order-history`, `order-detail`.

**Tamaño:** M.

### Archivos

| Archivo | Qué |
| --- | --- |
| `data/repository/UserRepository.kt` | `me()` (`GET users/me`) |
| `data/repository/OrderRepository.kt` | `orders()` y `order(id)` |
| `core/formatting/DisplayFormat.kt` | `Sep 15, 2026` y `Sep 15, 2026, 11:30 AM` (`en_US`, zona del dispositivo como parámetro), "1 item" / "N items", `CONFIRMED` → "Confirmed" |
| `features/account/AccountViewModel.kt` (ampliar), `AccountScreen.kt` | Perfil + tema (ya existe) + "My orders" + "Log out" |
| `features/orders/OrdersViewModel.kt`, `OrdersScreen.kt`, `OrderDetailViewModel.kt`, `OrderDetailScreen.kt` | Lista, detalle y "Order not found" |
| `app/AppContainer.kt` | `resetNavigation()` tras el logout |

### Actividades

- [ ] `logout()`: vaciar el carrito y `session.end()`, **sin** aviso de expiración; la pantalla reinicia la navegación (pestaña Catalog y pilas vacías). Con sesión expirada no se reinicia.
- [ ] "Order not found" con "OK" que vuelve a la pantalla anterior (`popBackStack()`).

### Tests

**Unitarios:** `DisplayFormatTest` con zona fija (`America/Bogota` y `Pacific/Auckland` cambian de día), unidades, estado, y que antes de AM/PM hay un espacio normal.

**Integración (`AccountIntegrationTest`, `OrdersIntegrationTest`):** `standard` → perfil; `logout()` → sesión cerrada sin aviso y carrito vacío; token expirado → `didExpire` y carrito conservado; `buyer` → órdenes 2 y 1 con totales `45.00` y `109.99` y unidades 1 y 3; `standard` → lista vacía; `buyer` abre la 1 (subtotales `59.99` y `50.00`, total `109.99`); `standard` abre la 1 y la 999 → `ORDER_NOT_FOUND`.

### Criterios de aceptación

- [ ] El tema cambia en vivo y se conserva; el logout vuelve al login sin aviso; una compra recién hecha aparece la primera en "My orders".

---

## Iteración A9 · `mobile-testing`

**Objetivo:** dejar la app lista para las suites de Maestro (y de Appium si se necesita) y cerrar la revisión de IDs.

**Referencias:** Arquitectura §10 y §11; [Guía de Maestro](../../ecommerce_shop_ios/docs/guia-pruebas-maestro-ios.md).

**Tamaño:** M.

### Actividades

**Revisión previa (antes de escribir o ejecutar cualquier flujo de Maestro)**

- [ ] Revisar con el responsable el estado de la app: IDs presentes frente al catálogo (`TestTagsTest` en las dos direcciones), pantallas ya validadas en emulador y pendientes de iOS que afecten a Android (flujos 04, 05, 07, 08, 09 y los hooks).
- [ ] Confirmar la lista de flujos y el emulador/API de partida. No se inicia ninguna prueba hasta tener esta revisión aprobada.

**Hooks de prueba (solo `debug`, con `BuildConfig.DEBUG`)**

- [ ] `MainActivity` lee los *extras* `apiBaseUrl`, `testSessionToken`, `testForceError`, `testForceErrorPath` y `testDelay` (Arquitectura §11) y se los pasa al `AppContainer`/`ApiClient` (cabeceras `X-Force-Error` y `X-Delay`).
- [ ] Tests de integración: alerta genérica con `X-Force-Error: 503` en catálogo, login y checkout (en checkout, "Try again" reabre "Place order?").

**Maestro (reutilizando la carpeta de iOS)**

- [ ] Parametrizar `appId` (variable de entorno en `run.sh`) y ajustar solo lo específico de Android: cómo se baja el teclado y cómo se toca el selector de cantidad (`add_to_cart_quantity_stepper-Increment`).
- [ ] Conceder el permiso de red local antes de cada flujo (`adb shell pm grant co.com.learningswift.shop android.permission.ACCESS_LOCAL_NETWORK` en `run.sh`, o `launchApp: permissions`), porque el diálogo del sistema taparía la app.
- [ ] Ejecutar los flujos `00` a `09` en un emulador API 34 y en uno actual, en claro y oscuro.

**Verificaciones propias de Android**

- [x] Los IDs de los botones de un `AlertDialog` llegan a `resource-id`: **verificado en la A2** (hizo falta `Modifier.semantics { testTagsAsResourceId = true }` en el `modifier` del `AlertDialog`). Queda confirmarlo con Maestro. Si no, repetir `testTagsAsResourceId` dentro del diálogo; si aun así no llegan, localizar las alertas por texto.
- [ ] El ID de las pestañas (`NavigationBarItem`) llega al árbol.
- [ ] La casilla (`stateDescription`) y los desplegables se ven en `maestro hierarchy`.

**Cierre**

- [ ] Reglas de R8 para `kotlinx.serialization` y Ktor en `proguard-rules.pro`, y **ejecutar** el APK de release en un emulador.
- [ ] Revisar `lint` y la tabla de compatibilidad del README.

### Criterios de aceptación

- [ ] Todos los flujos de Maestro pasan en los dos emuladores.
- [ ] Ningún ID del catálogo falta en la app ni hay IDs de la app que no estén en el catálogo (la prueba `TestTagsTest` se amplía en las dos direcciones).
- [ ] El APK de release minificado funciona.

---

## 7. Riesgos y qué hacer

| Riesgo | Qué hacer |
| --- | --- |
| Una versión de AGP/Kotlin/Compose nueva rompe la compilación | Subir una pieza cada vez, en el orden del README, y compilar y probar tras cada cambio |
| El comportamiento cambia en un repositorio y no en el otro | Un solo documento de comportamiento; se copia el mismo día |
| Los IDs no llegan en los `AlertDialog` | Resuelto en la A2 con `testTagsAsResourceId` en el `modifier` del diálogo; confirmar con Maestro en la A9 |
| El APK de release falla por R8 | Reglas en A9 y una ejecución real antes de publicar |
| Iteraciones demasiado grandes (A4, A5, A7) | Partirlas en dos *changes* (por ejemplo, A5 en imágenes+catálogo y en carrito-reglas), sin cambiar el orden |
| Se cuela un `catch (e: Exception)` | Revisión de código y la lista de la Arquitectura §15 |

## 8. Decisiones

| # | Decisión | Resultado |
| --- | --- | --- |
| 1 | ¿Fusionar `product-images` en `product-catalog` y `account` con `orders`? | **Decidido: sí** |
| 2 | ¿Dónde viven los documentos compartidos? | **Decidido:** cada repositorio con su copia y el mismo día de actualización; enlace relativo a la copia de iOS mientras tanto |
| 3 | ¿Appium en Android? | **Decidido:** se mantiene Maestro como única herramienta; antes de iniciar las pruebas se hace la revisión previa de la A9 |
| 4 | ¿Persistir el token en la fase 1? | Abierta. Recomendación: no (igual que iOS); el cifrado ya está en el proyecto base si se decide |
