# form-validation Specification

## Purpose
Define cómo se validan y se muestran los errores de campo en los formularios de la app (registro y, después, checkout): cuándo se valida, cómo se ve cada error, el resumen, el desplazamiento al primer error y el papel de los errores de campo del backend como red de seguridad.

## Requirements

### Requirement: Error en línea por campo
Cada error de campo SHALL mostrarse en rojo justo debajo de su control, con todas las líneas de ese campo (una por mensaje), y con el identificador `<pantalla>_<campo>_error_text`. El elemento MUST existir solo mientras el campo tiene un error.

#### Scenario: Un error
- **WHEN** el campo email tiene el error "Enter your email."
- **THEN** ese texto aparece en rojo bajo el email, localizable por `register_email_error_text`

#### Scenario: Varios mensajes en un campo
- **WHEN** un campo tiene dos mensajes de error
- **THEN** ambos aparecen bajo el campo, uno por línea, en el mismo elemento

#### Scenario: Campo corregido
- **WHEN** el campo ya no tiene error
- **THEN** `register_email_error_text` ya no existe

### Requirement: Resumen de errores
Cuando la pantalla visible del formulario tiene errores, la app SHALL mostrar arriba un resumen con el número de campos con error de esa pantalla: "1 field needs attention" o "N fields need attention" (`register_error_summary_text`). El resumen MUST NOT existir cuando la pantalla no tiene errores y MUST contar campos, no mensajes.

#### Scenario: Un campo con error
- **WHEN** solo un campo tiene error
- **THEN** el resumen dice "1 field needs attention"

#### Scenario: Tres campos con error
- **WHEN** tres campos tienen error, uno de ellos con dos mensajes
- **THEN** el resumen dice "3 fields need attention"

#### Scenario: Sin errores
- **WHEN** ningún campo tiene error
- **THEN** el resumen no existe

#### Scenario: Formulario en varias pantallas
- **WHEN** un formulario en dos pantallas tiene 2 errores en la primera y 1 en la segunda
- **THEN** la primera muestra "2 fields need attention" y la segunda "1 field needs attention"

### Requirement: Desplazamiento al primer error
Cuando un envío termina con errores, el formulario SHALL desplazarse hasta el primer campo con error, en el orden en que aparecen los campos, y ese campo MUST quedar visible aunque el teclado esté abierto.

#### Scenario: Error en un campo lejano
- **WHEN** el usuario pulsa "Create account" con solo el campo de términos con error
- **THEN** el formulario se desplaza hasta mostrar el error de términos

#### Scenario: Varios errores
- **WHEN** hay errores en el email y en los términos
- **THEN** el formulario se desplaza al email

### Requirement: Botón de envío siempre habilitado
El botón de envío, o el que avanza a la siguiente pantalla de un formulario con validación, SHALL estar siempre habilitado, salvo mientras hay una petición en curso, para que cada error pueda provocarse y comprobarse.

#### Scenario: Formulario vacío
- **WHEN** el formulario está sin rellenar
- **THEN** el botón de envío está habilitado y, al pulsarlo, muestra los errores

### Requirement: Cuándo se valida
Al pulsar el botón de envío, la app SHALL validar todos los campos y, si hay errores, MUST NOT llamar al backend. Al pulsar el botón que avanza a la siguiente pantalla, SHALL validar los campos de la pantalla actual y no avanzar si hay errores. Tras ese primer intento, cada campo SHALL revalidarse al salir de él. El error de un campo MUST desaparecer en cuanto el usuario lo edita.

#### Scenario: Envío con errores
- **WHEN** hay errores locales y se pulsa el botón
- **THEN** no se envía ninguna petición al backend

#### Scenario: Revalidar al salir del campo
- **WHEN** ya hubo un intento de envío y el usuario sale del campo email dejándolo vacío
- **THEN** aparece "Enter your email." sin pulsar de nuevo el botón

#### Scenario: Sin intento previo
- **WHEN** todavía no se ha pulsado el botón y el usuario sale de un campo vacío
- **THEN** no aparece ningún error

#### Scenario: Editar quita el error
- **WHEN** un campo tiene un error y el usuario escribe en él
- **THEN** el error de ese campo desaparece

### Requirement: La app no recorta lo que se escribe
La app SHALL validar lo escrito, pero MUST NOT recortar ni bloquear la entrada del usuario (por ejemplo, impedir escribir más de 30 caracteres), para que todos los errores puedan provocarse.

#### Scenario: Texto más largo que el máximo
- **WHEN** el usuario escribe 31 caracteres en el nombre
- **THEN** los 31 caracteres se aceptan y el error aparece al validar

### Requirement: Errores de campo del backend como red de seguridad
Si el backend responde `VALIDATION_ERROR` con errores de campo, la app SHALL mostrarlos tal como llegan bajo su campo, sustituyendo a los errores locales, con todos los mensajes de cada campo. Si ningún error corresponde a un campo del formulario (incluido un `VALIDATION_ERROR` sin errores de campo), MUST mostrarse la alerta genérica. Si hay errores con y sin campo, los que tienen campo van en línea y además se muestra la alerta genérica.

#### Scenario: Campo conocido
- **WHEN** el backend responde `VALIDATION_ERROR` con un error para `firstName`
- **THEN** el mensaje aparece bajo el campo de nombre

#### Scenario: Campo con otro nombre en el backend
- **WHEN** el backend responde un error para `experienceLevel` o `termsAccepted`
- **THEN** aparece bajo el campo de experiencia o el de términos, respectivamente

#### Scenario: Campo desconocido
- **WHEN** el backend responde `VALIDATION_ERROR` solo con un campo que el formulario no tiene
- **THEN** se muestra la alerta genérica y ningún error en línea

#### Scenario: Sin errores de campo
- **WHEN** el backend responde `VALIDATION_ERROR` con `fieldErrors` vacío
- **THEN** se muestra la alerta genérica
