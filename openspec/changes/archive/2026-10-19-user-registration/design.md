# Diseño: user-registration

## Contexto

Se reutiliza: `ApiClient.post(..., headers)`, `ErrorMessages` (`EMAIL_ALREADY_EXISTS`, `UNDERAGE_USER`, `TERMS_NOT_ACCEPTED`), `AppError.Api.fieldErrors` (ya agrupado por campo), `ForcedError`, `GenericErrorDialog`, `LoadingOverlay`, `MessageBanner`, `LoginViewModel` (a nivel de `AuthHost`, sobrevive a la navegación) y `TestTags`/`AlertText`. Falta `AlertText.AccountCreated`.

Backend (`POST users`): 201 con el usuario; 400 `VALIDATION_ERROR`; 422 `UNDERAGE_USER` y `TERMS_NOT_ACCEPTED`; 409 `EMAIL_ALREADY_EXISTS`. Orden: formato → edad → términos → email repetido. Un teléfono vacío es 400 (la app ya lo exige). Para quien nace un 29 de febrero, el backend no lo considera mayor de edad hasta el 1 de marzo.

Código de referencia: Arquitectura §8 (formularios) y §5.6 a §5.8. Diseño de iOS: `../ecommerce_shop_ios/openspec/changes/archive/2026-10-18-user-registration/design.md`.

## Decisiones

### 1. Modelos

- `Country` (`enum class`: `AR, CL, CO, MX, PE, US`, con `displayName` en inglés y `sorted`), `Plan` (`BASIC`, `PRO`, con `label`). Sin `ES`.
- `User`/`UserDto`: **`country` es `String`**, no `Country`: el backend devuelve también `ES` y un usuario creado desde otro cliente haría fallar la decodificación. `plan` sí es `Plan`. `createdAt` y `termsAcceptedAt` se leen como texto: las fechas con hora llegan en A8 (órdenes).

### 2. La fecha: `java.time`, sin zonas horarias

Más simple que iOS: el formulario guarda `birthDay`, `birthMonth` y `birthYear` como enteros (1 / 1 / 2000). No hay `Date` ni zonas que puedan mover el día.

- Se envía `String.format(Locale.ROOT, "%04d-%02d-%02d", y, m, d)` (o `LocalDate.toString()`): no depende del idioma.
- `BirthDateOptions` (puro): meses en inglés fijos, años del actual a 1900, `daysInMonth(year, month)` con `YearMonth.lengthOfMonth()`, `clampedDay(day, year, month)`.
- **Mayoría de edad:** se compara la **tupla** `(añoNac + 18, mes, día) <= (añoHoy, mesHoy, díaHoy)` con `today = LocalDate.now(ZoneOffset.UTC)`. **No usar `LocalDate.plusYears(18)`**: para el 29 de febrero devuelve el 28 y el usuario pasaría por mayor un día antes que en el backend (el 2026-02-28 con `2008-02-29` debe dar menor de edad).

### 3. `RegisterField`, `RegisterForm` y `RegisterValidator` (puros)

- `RegisterField` (enum en el orden del formulario): `FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, PHONE, BIRTH_DATE, COUNTRY, PLAN, EXPERIENCE, TERMS`, con `step` (`BASICS` para los cinco primeros), `tagSuffix` (`firstname`, `birthdate`…) y `backendName` (`firstName`, `birthDate`, `experienceLevel`, `termsAccepted`…) y `fromBackend(name)`. El orden define "el primer campo con error".
- `RegisterForm` con los valores iniciales de la spec.
- `RegisterValidator.validate(form, today: LocalDate): Map<RegisterField, String>`: un mensaje por campo (el primero que falle). Recorte con `trim()` en textos, no en la contraseña. Regex: nombres `^[A-Za-z]+( [A-Za-z]+)*$`, email caracteres `[A-Za-z0-9@.]` y forma `^[A-Za-z0-9]+@[A-Za-z0-9]+(\.[A-Za-z0-9]+)+$`, teléfono `^\+[0-9]{13}$`, contraseña con rango `!`..`~` y las cuatro clases. Los mensajes son los de la spec (copiados de iOS).
- La app puede ser más estricta que el backend (cada regla es un subconjunto de la suya), nunca más laxa.

### 4. `FieldErrorMapper` (genérico)

```kotlin
data class MappedFieldErrors<F>(val fields: Map<F, List<String>>, val hasUnmappedErrors: Boolean)
object FieldErrorMapper { fun <F> map(backend: Map<String, List<String>>, field: (String) -> F?): MappedFieldErrors<F> }
```

Un nombre que `field` no reconoce, o un mapa vacío, activa `hasUnmappedErrors`. Lo reutiliza el checkout.

### 5. Datos

- `RegisterRequestDto`: `fromForm(form)` recorta textos, pasa el email a minúsculas, **quita el `+`** del teléfono, envía el país como código, el plan como `BASIC`/`PRO` y `birthDate` como texto.
- `UserRepository.register(form): User` hace `POST users` con `headers = ForcedError.headersFor(email)`. No toca la sesión.
- `AppContainer.userRepository`.

### 6. `RegisterViewModel` único para las dos pantallas

`RegisterUiState` guarda el formulario, `fieldErrors: Map<RegisterField, List<String>>`, `hasSubmitted`, `isLoading`, `scrollTick` (se incrementa cuando hay que desplazarse), `registeredEmail`, `genericError`. Funciones públicas (verbos): `onFirstNameChange`… por campo, `onBirthDayChange/Month/Year` (el día se ajusta con `clampedDay`), `continueToDetails(): Boolean`, `submit(): Job`, `onFieldBlur(field)`, `dismissGenericError()`, `consumeRegisteredEmail()`.

- Editar un campo borra su error; cambiar día, mes o año borra el de la fecha.
- `continueToDetails()`: `hasSubmitted = true`, valida todo y conserva solo los errores del paso 1; si hay, sube `scrollTick` y devuelve `false` sin llamar al backend.
- `submit()`: ignora un segundo envío en curso, valida todo, no llama al backend si hay errores, y tras colocar errores la pantalla vuelve al paso del primer campo con error (`stepOfFirstError`).
- Errores del backend: `EMAIL_ALREADY_EXISTS` → email; `UNDERAGE_USER` → fecha; `TERMS_NOT_ACCEPTED` → términos; `VALIDATION_ERROR` → `FieldErrorMapper` (con errores sin campo se añade la alerta genérica); cualquier otro → alerta genérica. "Try again" llama a `submit()` (es seguro: un registro ya creado responde "email ya registrado").
- `onFieldBlur` revalida ese campo solo si `hasSubmitted`.

### 7. Navegación y alcance del ViewModel

`AuthHost` pasa a un `NavHost` con `login` y un **grafo anidado `register_graph`** con `register/basic` y `register/details`.

- `LoginViewModel` se sigue creando en `AuthHost` (fuera del `NavHost`): conserva email y contraseña al ir al registro y volver.
- `RegisterViewModel` se crea con el `NavBackStackEntry` del grafo: `val parent = remember(entry) { navController.getBackStackEntry("register_graph") }; viewModel(parent, factory = …)`. Así lo comparten las dos pantallas, sobrevive a la flecha atrás entre ellas y **se destruye al salir del grafo**, de modo que cada visita al registro empieza con los valores iniciales.
- "Next" válido → `navigate("register/details")`. Error de un campo del paso 1 tras enviar → `popBackStack()` (vuelve al paso 1). "Log in" de la alerta de éxito → se pone el email en `LoginViewModel`, contraseña vacía y `popBackStack("login", inclusive = false)`.

### 8. Componentes y pantallas

- `core/design`: `FieldErrorText(lines, tag)` (una línea por mensaje, solo existe con error), `ErrorSummary(count)` ("1 field needs attention"/"N fields need attention"), `LabeledCheckbox(checked, label, tag)` con `Role.Checkbox` y `stateDescription` `checked`/`unchecked`.
- `BirthDateField`: tres filas dentro de una misma tarjeta (Day, Month, Year; etiqueta a la izquierda, valor a la derecha), cada una con `ExposedDropdownMenuBox` y su ID; el error de la fecha una sola vez debajo.
- Paso 1 y paso 2 con `Column(verticalScroll, imePadding)`. Desplazamiento al primer error con `BringIntoViewRequester` por campo, disparado por `scrollTick` y al aparecer la pantalla. País: `ExposedDropdownMenuBox` con "Select" + 6 países. Plan: `SingleChoiceSegmentedButtonRow`. Experiencia: `Slider(valueRange = 1f..5f, steps = 3)` con el valor visible. Newsletter: `Switch`.
- Los textos con `KeyboardOptions` (email, contraseña, teléfono) y `FocusChanged` → `onFieldBlur`. Los `testTag` van en el campo, no en un contenedor.
- La alerta "Account created" es un `AlertDialog` con `Modifier.semantics { testTagsAsResourceId = true }` (como la genérica).

### 9. Pruebas

- **Unitarias sin red:** `CountryTest`, `RegisterFieldTest`, `RegisterValidatorTest` (tablas de límites y `today` fijo), `FieldErrorMapperTest`, `RegisterRequestDtoTest`, `BirthDateOptionsTest`, `RegisterViewModelTest` (con un `ApiClient` a `http://localhost:9/api/v1/` para detectar llamadas indebidas).
- **Integración (`RegistrationIntegrationTest`, backend real):** registro válido → `User` con `ACTIVE` y login posterior; teléfono vacío directo → 400; `standard`/`Standard` → `EMAIL_ALREADY_EXISTS` y `stepOfFirstError == BASICS`; menor de edad; términos; `force503@…` → alerta genérica; doble envío = una cuenta; "Try again" reenvía.
- La parte visual se verifica en el emulador y con previews.

## Riesgos

- **IDs de menús, slider y casilla:** `ExposedDropdownMenuBox`, `Slider` y `SingleChoiceSegmentedButtonRow` pueden no propagar el `testTag` al nodo que Maestro busca. Se revisa con `uiautomator dump`; si no llega, se anota en el catálogo (como las pestañas) y se corrige en A9.
- **Teclado numérico del teléfono:** no tiene tecla de retorno; "Next" debe quedar visible sobre él (`imePadding` + scroll).
- **Autorrelleno de contraseñas** del emulador: apagarlo para las pruebas de interfaz.
- **Permiso de red local (Android 17):** ya resuelto en A3.
- **Tamaño de la iteración (L):** las tareas van por capas (reglas → datos → ViewModel → pantallas) y cada una se verifica antes de seguir.
