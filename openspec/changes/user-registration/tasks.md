# Tareas: user-registration

Verificación (desde la raíz del repositorio):

- **UNIT:** `./gradlew :app:testDebugUnitTest --tests '<Clase>'` (sin red).
- **ALL:** `./gradlew :app:testDebugUnitTest`. **Requiere el backend levantado** (`docker compose up` en `../ecommerce_shop_api`, perfil `testing`; `curl http://localhost:8080/actuator/health` responde `UP`).
- **BUILD:** `./gradlew :app:assembleDebug :app:lintDebug`.
- **IDs:** `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml`.

IDs a revisar en la tarea 6.3: los `register_*` del catálogo (incluidos `register_next_button` y los tres desplegables de la fecha), los `register_<campo>_error_text` cuando hay error, `register_success_alert_login_button`, `app_loading_indicator` y los botones de la alerta genérica.

Los datos válidos de los tests son los de iOS: `Alexandria` / `Montgomery` / `ana@example.com` / `Secret_123` / `+5733001234567` / país Colombia.

## 1. Modelos y reglas de validación (sin red)

- [x] 1.1 Escribir `CountryTest` y `PlanTest` (fallan): seis países con sus códigos (`AR`, `CL`, `CO`, `MX`, `PE`, `US`), nombres en inglés, `Country.sorted` alfabético (Argentina, Chile, Colombia, Mexico, Peru, United States) y sin `ES`; `Plan.BASIC` y etiquetas "Basic" y "Pro". Crear `data/model/Country.kt` y `Plan.kt`. Verificar: UNIT falla por no compilar y después pasa.
- [x] 1.2 Escribir `RegisterFieldTest` (falla): orden de los casos (nombre, apellido, email, contraseña, teléfono, fecha, país, plan, experiencia, términos), `step` (los cinco primeros `BASICS`), `tagSuffix` y `backendName` (`firstname`/`firstName`, `birthdate`/`birthDate`, `experience`/`experienceLevel`, `terms`/`termsAccepted`), `fromBackend("experienceLevel") == EXPERIENCE` y un nombre desconocido da `null`. Crear `features/register/RegisterField.kt` y `RegisterForm.kt` (valores iniciales de la spec). Verificar: UNIT.
- [x] 1.3 Escribir `BirthDateOptionsTest` (falla): 12 meses en inglés en orden; años del actual a 1900 sin repetidos; `daysInMonth` de enero 31, abril 30, febrero de 2023 28, de 2024 29, de 2000 29 y de 1900 28; `clampedDay(31, 2000, 2) == 29`, `(31, 2001, 2) == 28`, `(15, 2001, 2) == 15`. Crear `BirthDateOptions.kt`. Verificar: UNIT.
- [x] 1.4 Escribir `RegisterValidatorTest` (falla), con tablas de casos y `today` fijo:
  - nombre y apellido: vacío, solo espacios, 9, 10, 30 y 31 letras, con número, con tilde, con guion, dos espacios seguidos, `Ana Maria Lopez` y `" ana maria lopez "` válidos, el orden vacío → caracteres → longitud (`Ana1` da solo el de caracteres);
  - email: vacío, `ana_123@x.com`, `ana-1@x.com`, `ana+1@x.com`, `ana 1@x.com`, `anaxxxxxxx`, `ana@xxxxxx`, `ana@@x.com`, `@x.com`, `ana@.com`, 7 y 21 caracteres, `ana@x.co` (8) y `abcdefghij@abcdef.co` (20) válidos, `"  ANA@X.COM "` válido;
  - contraseña: vacía; 9, 10, 20 y 21 caracteres; sin mayúscula, sin minúscula, sin dígito, sin especial; con espacio; con `ñ`; `Secret_123` válida; `" Secret_123"` falla (no se recorta);
  - teléfono: vacío, `3001234567`, `+573300123456`, `+57330012345678`, `+57 3300 1234567`, `+57-3300123456`, y `+5733001234567` y `" +5733001234567 "` válidos;
  - edad: cumple 18 hoy (2026-10-11 con 2008-10-11, válido), mañana (2008-10-12, error), `2008-02-29` con hoy `2026-02-28` (error) y con `2026-03-01` (válido), y 2000-01-01 válida;
  - país sin seleccionar y términos sin marcar;
  - un formulario válido completo devuelve un mapa vacío.

  Crear `RegisterValidator.kt` (design.md §2 y §3; la edad con la **tupla**, no con `plusYears`). Verificar: UNIT falla por no compilar y después pasa.
- [x] 1.5 Escribir `FieldErrorMapperTest` (falla): cada campo del backend va a su `RegisterField`; un campo con dos mensajes conserva los dos; un campo desconocido activa `hasUnmappedErrors` pero los conocidos se mapean; un mapa vacío activa `hasUnmappedErrors`. Crear `core/errors/FieldErrorMapper.kt` (genérico). Verificar: UNIT.

## 2. Datos y repositorio

- [x] 2.1 Escribir `RegisterRequestDtoTest` (falla): `birthDate` `"2000-01-01"` con el formulario inicial, aunque el dispositivo use otra zona o idioma (`Locale`/`TimeZone` por defecto cambiados dentro del test); `+5733001234567` se envía como `5733001234567`; email `"  Ana@X.COM "` sale `ana@x.com`; nombres recortados con su espacio interior; plan `BASIC`/`PRO`; país como código; `experienceLevel` entero; booleanos como booleanos. Crear `RegisterRequestDto.kt`, `UserDto.kt` y `data/model/User.kt` (con `country` como `String`). Verificar: UNIT.
- [x] 2.2 Añadir `AlertText.AccountCreated` (título "Account created", mensaje "You can now log in with your email.", botón "Log in") y `TestTags.Register.*` (los 19 IDs del catálogo, los diez `*_error_text` y `register_success_alert_login_button`). Verificar: `TestTagsTest` pasa (todo ID existe en el catálogo).
- [x] 2.3 Escribir `RegistrationIntegrationTest` (falla), contra el repositorio y con reset antes de cada test (**requiere backend**): formulario válido → `User` con `status` `ACTIVE` y luego `login` inicia sesión; `standard@shoplab.dev` y `Standard@ShopLab.dev` → `Api(409, "EMAIL_ALREADY_EXISTS")`; fecha de 2020 → `Api(422, "UNDERAGE_USER")`; términos sin marcar → `Api(422, "TERMS_NOT_ACCEPTED")`; un teléfono `300-123` enviado directo → `Api(400, "VALIDATION_ERROR")` con `fieldErrors["phone"]`. Crear `UserRepository.kt` (`POST users` con `ForcedError.headersFor`) y `AppContainer.userRepository`. Verificar: ALL falla por no compilar y después pasa.

## 3. ViewModel

- [x] 3.1 Escribir `RegisterViewModelTest` (falla), sin red (`ApiClient` a `http://localhost:9/api/v1/`): valores iniciales (1 / 1 / 2000, Basic, 3, newsletter apagada); con día 31, cambiar el mes a febrero deja 29 en 2000 y 28 en 2001, y cambiar el año de 2000 a 2001 con el 29 de febrero lo baja a 28; editar un campo borra su error; cambiar día, mes o año borra el de la fecha; `continueToDetails()` con el paso 1 vacío devuelve `false`, deja errores en los cinco campos y no llama al backend; con el paso 1 válido devuelve `true` sin errores; `onFieldBlur` solo revalida tras el primer intento; `errorCount(step)` por pantalla; `submit()` con errores no llama al backend y `stepOfFirstError` apunta al paso correcto; con errores del backend simulados mediante `handle(error)`: `EMAIL_ALREADY_EXISTS` → email y `BASICS`, `UNDERAGE_USER` → fecha, `TERMS_NOT_ACCEPTED` → términos, `VALIDATION_ERROR` con `experienceLevel` → experiencia, con campo desconocido o vacío → alerta genérica, con errores con y sin campo → en línea y alerta. Crear `RegisterUiState.kt`. Verificar: UNIT falla por no compilar.
- [x] 3.2 Ampliar `RegistrationIntegrationTest` (falla) con el ViewModel contra el backend real (**requiere backend**): formulario válido deja `registeredEmail` recortado y en minúsculas y luego el login funciona; `standard@shoplab.dev` deja "An account with this email already exists." en el email, sin alerta, y `stepOfFirstError == BASICS`; un formulario con errores locales no crea la cuenta (el mismo email se registra después); `force503@shoplab.dev` con datos válidos → alerta genérica 5xx sin cuenta; doble `submit()` seguido crea una sola cuenta; "Try again" (`submit()` otra vez) tras un error forzado repite la alerta. Verificar: ALL falla.
- [x] 3.3 Crear `RegisterViewModel.kt` (design.md §6) y su fábrica en `ViewModelFactories.kt`. Verificar: `RegisterViewModelTest` pasa con UNIT y los casos de 3.2 con ALL (**requiere backend**).

## 4. Componentes visuales

- [x] 4.1 Crear `core/design/FieldErrorText.kt`, `ErrorSummary.kt` y `LabeledCheckbox.kt` (design.md §8) con `@Preview` en claro y oscuro: error con una y con dos líneas, resumen con 1 y con 3, casilla marcada y sin marcar. Verificar: BUILD pasa y los previews muestran "1 field needs attention", "3 fields need attention" y los dos estados de la casilla.
- [x] 4.2 Crear `features/register/BirthDateField.kt`: tres filas en una tarjeta con `ExposedDropdownMenuBox` (Day, Month, Year; etiqueta a la izquierda, valor a la derecha, el nombre del mes completo visible aunque sea "September"), la lista de días ajustada al mes y al año, sus tres IDs y el error de la fecha una sola vez debajo. Incluir `@Preview` claro y oscuro. Verificar: BUILD pasa y el preview muestra las tres filas.

## 5. Pantallas y navegación

- [x] 5.1 Crear `RegisterBasicScreen.kt` (paso 1): "Step 1 of 2", resumen del paso, nombre, apellido, email (teclado de email, sin mayúscula), contraseña (oculta), teléfono (teclado de teléfono, ayuda `+5733001234567`), "Next" (`register_next_button`, siempre habilitado) y el enlace al login (`register_login_link`). `Column` con `verticalScroll` e `imePadding`, desplazamiento al primer error del paso (`scrollTick` y al aparecer) y `onFieldBlur` al perder el foco. Con `@Preview` claro, oscuro y con errores. Verificar: BUILD pasa.
- [x] 5.2 Crear `RegisterDetailsScreen.kt` (paso 2): "Step 2 of 2", resumen, `BirthDateField`, país (`ExposedDropdownMenuBox`: "Select" + seis países), plan (segmentado Basic/Pro), experiencia (slider 1 a 5, `steps = 3`, valor visible), newsletter (`Switch`), términos (`LabeledCheckbox`) y "Create account" (`register_submit_button`). `LoadingOverlay`, `GenericErrorDialog` con `onRetry = submit()` y la alerta "Account created" (`AlertDialog` con `testTagsAsResourceId` y `register_success_alert_login_button`). Si tras enviar el primer error es del paso 1, avisa para volver. Verificar: BUILD pasa.
- [x] 5.3 Convertir `AuthHost` en un `NavHost` (design.md §7): `login`, y el grafo `register_graph` con `register/basic` y `register/details`; `RegisterViewModel` con el `NavBackStackEntry` del grafo; "Create account" del login abre el paso 1; "Next" abre el paso 2; el error del paso 1 hace `popBackStack()`; el enlace del paso 1 y la flecha atrás vuelven al login conservando lo escrito; "Log in" de la alerta vuelve al login con el email y la contraseña vacía. Verificar: BUILD pasa y la app abre en el login.

## 6. Validación en el emulador (backend levantado)

- [x] 6.1 Ejecutar ALL, BUILD y `assembleRelease`. Verificar: todo pasa, incluidos los de `bootstrap`, `app-shell` y `user-login`.
- [x] 6.2 Con el autorrelleno de contraseñas del emulador apagado y el teclado abierto, en claro y oscuro:
  - "Next" con el paso 1 vacío: resumen "5 fields need attention", error bajo cada campo, desplazamiento al primero y sin avanzar;
  - las reglas: nombre de 9 letras, con espacio doble y con tilde; email con `_`, de 7 y de 21 caracteres; contraseña de 9 caracteres; teléfono sin `+` y con 12 dígitos; y los valores del límite válidos;
  - corregir un campo (su error desaparece) y salir de otro vacío tras el primer intento (aparece);
  - avanzar y volver con la flecha atrás: lo escrito se conserva; volver al login y reabrir el registro: todo en sus valores iniciales;
  - los tres desplegables de la fecha: elegir 31 y luego February (el día baja a 28 o 29) y una fecha de menor de edad (error una vez, bajo los tres);
  - "Create account" sin país ni términos: "2 fields need attention" en el paso 2;
  - registro válido → "Account created" → "Log in" → login con el email escrito → entrar con esa cuenta;
  - `standard@shoplab.dev` (vuelve al paso 1 con el error en el email);
  - `force503@shoplab.dev` con datos válidos (alerta genérica y "Try again");
  - el botón "Next" visible sobre el teclado numérico del teléfono.

  Verificar: cada caso se comporta como pide `specs/user-registration/spec.md` y `specs/form-validation/spec.md`; anotar lo que difiera y corregirlo.
- [x] 6.3 Revisar los IDs con `uiautomator dump`: los `register_*`, los `register_<campo>_error_text` y el botón de la alerta de cuenta creada; en especial los tres desplegables de la fecha, el de país, el segmentado, el slider y la casilla (`stateDescription`). Si alguno no llega, anotarlo en `docs/catalogo-ids-accesibilidad.md` (las tres copias) y en el plan A9. Verificar: la nota existe o los IDs se encuentran.
- [x] 6.4 Actualizar `docs/plan-iteraciones-android.md` (criterios de la A4) y `docs/shop-android-arquitectura.md` si alguna decisión cambió (por ejemplo, el alcance del `RegisterViewModel`). Verificar: casillas marcadas.

## Seguimiento

- El responsable revisa y hace el commit; después se archiva con `/opsx:archive`.

## Resultados de la verificación

- 141 tests JVM pasan (incluidos los de integración contra el backend real); `assembleDebug`, `assembleRelease` y `lintDebug` sin errores.
- Emulador (Pixel 6a, Android 17) con el autorrelleno desactivado (`adb shell settings put secure autofill_service null`): "Next" vacío → "5 fields need attention" con su error bajo cada campo; reglas de nombre y email con sus mensajes; avanzar y volver con la flecha atrás conserva lo escrito; reabrir el registro desde el login lo muestra vacío; Day pasa de 31 a 29 al elegir February (2000); fecha de 2020 → error de edad una sola vez bajo los tres desplegables; "Create account" sin país ni términos → resumen del paso 2; registro válido → "Account created" → "Log in" → login con el email escrito y contraseña vacía → entra con esa cuenta (llega a Catalog); `standard@shoplab.dev` vuelve al paso 1 con "An account with this email already exists." y "1 field needs attention"; `force503@shoplab.dev` → alerta genérica, "Try again" la repite y "Cancel" conserva el formulario; claro y oscuro legibles.
- IDs (`uiautomator dump`): llegan todos los `register_*`, los `register_<campo>_error_text` (solo mientras hay error), `register_success_alert_login_button`, los tres desplegables, el segmentado, el slider, el switch y la casilla (con `checkable`/`checked`: la casilla se lee como `checked="true"` tras marcarla).
- **Hallazgos:**
  - El teclado numérico del teléfono tapaba casi todo el botón "Next": se configuró `ImeAction.Done` y su ✓ cierra el teclado (`clearFocus`).
  - El teclado del emulador autocorregía ("Alexandria" → "Alexandria's "): `FormTextField` y el email del login desactivan `autoCorrectEnabled`.
  - Con el teclado abierto, los campos de abajo quedan tapados: para automatizar hay que cerrar el teclado entre campos (Maestro lo hace con `hideKeyboard`).
