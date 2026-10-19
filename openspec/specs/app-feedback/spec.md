# app-feedback Specification

## Purpose
Define los avisos comunes a todas las pantallas de la app: el indicador de carga, el mensaje temporal (snackbar) y la alerta genérica de error con "Try again" y "Cancel".

## Requirements

### Requirement: Indicador de carga
Mientras una pantalla carga datos, la app SHALL mostrar un indicador de carga a pantalla completa con el identificador de accesibilidad `app_loading_indicator`. El indicador MUST existir solo mientras carga: cuando termina, no queda en pantalla ni en el árbol de accesibilidad.

#### Scenario: Cargando
- **WHEN** una pantalla está cargando
- **THEN** se ve el indicador de carga y se localiza por `app_loading_indicator`

#### Scenario: Carga terminada
- **WHEN** la carga termina
- **THEN** el indicador desaparece y `app_loading_indicator` ya no existe

### Requirement: Mensaje temporal (snackbar)
La app SHALL poder mostrar un mensaje de texto temporal en la parte inferior de las pestañas principales, por encima de la barra de pestañas, con el identificador `app_snackbar_text`. El mensaje MUST desaparecer solo a los 3 segundos. Un mensaje nuevo SHALL reemplazar al que esté visible y reiniciar los 3 segundos.

#### Scenario: Mostrar un mensaje
- **WHEN** una pantalla pide mostrar el mensaje "Added to cart"
- **THEN** el texto "Added to cart" aparece en la parte inferior, localizable por `app_snackbar_text`

#### Scenario: Desaparece solo
- **WHEN** pasan 3 segundos desde que apareció el mensaje y no llegó otro
- **THEN** el mensaje desaparece y `app_snackbar_text` ya no existe

#### Scenario: Un mensaje reemplaza a otro
- **WHEN** hay un mensaje visible y se pide mostrar otro
- **THEN** solo se ve el nuevo, y el anterior no vuelve a aparecer

#### Scenario: El mensaje viejo no apaga al nuevo
- **WHEN** se pide un mensaje 2 segundos después de otro
- **THEN** el nuevo permanece 3 segundos completos desde que apareció

### Requirement: Alerta genérica de error
Cuando una operación falla por un error que no tiene su propia pantalla ni alerta, la app SHALL mostrar una alerta de Material (`AlertDialog`) con el título "Something went wrong", el mensaje de usuario del error (nunca el mensaje técnico del backend) y dos botones: "Try again" (`app_error_alert_retry_button`) y "Cancel" (`app_error_alert_cancel_button`).

#### Scenario: Error de red
- **WHEN** una operación falla por falta de conexión
- **THEN** aparece la alerta "Something went wrong" con el mensaje "Check your internet connection and try again." y los botones "Try again" y "Cancel"

#### Scenario: Error del backend
- **WHEN** una operación falla con un error del backend
- **THEN** el mensaje de la alerta es el mensaje de usuario que corresponde al error, no el `message` del backend

#### Scenario: Pulsar "Try again"
- **WHEN** el usuario pulsa "Try again"
- **THEN** la alerta se cierra, el error se descarta y la pantalla ejecuta su acción de reintento

#### Scenario: Pulsar "Cancel"
- **WHEN** el usuario pulsa "Cancel"
- **THEN** la alerta se cierra, el error se descarta y no se ejecuta ningún reintento

#### Scenario: Sin error
- **WHEN** no hay ningún error pendiente
- **THEN** la alerta no se muestra

### Requirement: Avisos legibles en claro y oscuro
El indicador de carga, el snackbar y la alerta genérica SHALL verse bien y ser legibles en apariencia clara y oscura.

#### Scenario: Modo oscuro
- **WHEN** el dispositivo está en modo oscuro y se muestra cualquiera de los tres avisos
- **THEN** el aviso usa colores de la variante oscura y su texto es legible
