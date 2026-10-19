# Propuesta: user-registration

## Why

El login ya funciona, pero "Create account" no hace nada: sin registro no se puede crear un usuario nuevo desde la app. Es además la primera iteración con formularios largos, y fija el patrón de validación (validador puro, errores por campo, resumen, desplazamiento al primer error) que reutilizará el checkout. Es la iteración A4 de `docs/plan-iteraciones-android.md`; el comportamiento es el de iOS ya validado y endurecido (Comportamiento §5).

## What Changes

- **Registro en dos pantallas** con indicador de paso: paso 1 (nombre, apellido, email, contraseña, teléfono, "Next") y paso 2 (fecha de nacimiento en tres desplegables, país, plan, experiencia, newsletter, términos, "Create account"). Un solo `RegisterViewModel` para las dos.
- **Validación local con las reglas endurecidas de iOS:** nombres de 10 a 30 letras con un espacio entre palabras, email de 8 a 20 caracteres solo con letras, números, `@` y puntos, teléfono `+` + 13 dígitos, contraseña de 10 a 20 caracteres ASCII con las cuatro clases, mayoría de edad contra hoy en UTC, país y términos obligatorios.
- **Patrón de formulario:** botón siempre habilitado, resumen "N fields need attention", error bajo cada campo, desplazamiento al primer error, revalidación al salir del campo tras el primer intento y el error desaparece al editar.
- **Envío al backend** (`POST users`): teléfono sin `+`, email en minúsculas, `birthDate` como `yyyy-MM-dd`. Errores de negocio en su campo (`EMAIL_ALREADY_EXISTS` vuelve al paso 1), `VALIDATION_ERROR` como red de seguridad, y el resto con la alerta genérica ("Try again" reenvía).
- **Alerta "Account created"** con "Log in": vuelve al login con el email escrito y la contraseña vacía; no inicia sesión sola.
- **Error forzado** `force<400–599>@…` también en el registro (solo debug).
- **Navegación:** el flujo de autenticación pasa a un `NavHost` (login → registro paso 1 → paso 2). "Create account" del login abre el registro.
- **Piezas comunes** reutilizables por el checkout: `FieldErrorText`, `ErrorSummary`, `LabeledCheckbox`, `FieldErrorMapper`, `Country`, `Plan`.
- **IDs `Register.*`** en `TestTags` (los 19 del catálogo, más los `*_error_text` y el botón de la alerta).

## Capabilities

### New Capabilities

- `user-registration`: qué pide el registro, valores iniciales, reglas de validación, qué se envía, cómo se muestra cada respuesta y cómo se vuelve al login.
- `form-validation`: cómo se validan y muestran los errores de campo en los formularios (registro y, después, checkout).

### Modified Capabilities

- `user-login`: el requisito "Enlace a registro" deja de ser un no-op: abre el registro y conserva lo escrito en el login.

## Impact

- **Código nuevo:** `data/model/{Country,Plan,User}.kt`, `data/remote/dto/{RegisterRequestDto,UserDto}.kt`, `data/repository/UserRepository.kt`, `core/errors/FieldErrorMapper.kt`, `features/register/*`, `core/design/{FieldErrorText,ErrorSummary,LabeledCheckbox}.kt`.
- **Código modificado:** `app/AuthHost.kt` (NavHost), `app/AppContainer.kt` (`userRepository`), `app/ViewModelFactories.kt`, `core/errors/AlertText.kt` (`AccountCreated`), `core/testing/TestTags.kt`.
- **Tests:** unitarios sin red (validador, campos, opciones de fecha, DTO, mapper, ViewModel) e integración `RegistrationIntegrationTest` contra el backend real (**necesita `docker compose up`**).
- **Dependencias:** ninguna nueva (la fecha usa `java.time`, disponible desde minSdk 26).
- **Fuera de alcance:** pantalla de términos, validación en vivo contra el backend, iniciar sesión tras registrar, España en la lista de países.
