# Spec Delta

## Purpose
Define la pantalla de login: cómo el usuario entra a la app con su email y contraseña, qué ve ante cada respuesta del backend y cómo se le avisa cuando llega aquí por una sesión expirada.

## ADDED Requirements

### Requirement: Pantalla de login
Cuando no hay sesión, el flujo de autenticación SHALL mostrar la pantalla de login con un campo de email (`login_email_input`), un campo de contraseña oculta (`login_password_input`), el botón "Log in" (`login_submit_button`) y el enlace "Create account" (`login_register_link`). El campo de email MUST usar el teclado de email y no poner mayúscula automática; la tecla de acción MUST pasar del email a la contraseña ("Next") y enviar el formulario desde la contraseña ("Go") si está habilitado.

#### Scenario: Arranque sin sesión
- **WHEN** se abre la app y no hay sesión
- **THEN** se ve la pantalla de login con los dos campos, el botón "Log in" y el enlace "Create account"

#### Scenario: Teclado con el botón visible
- **WHEN** el teclado está abierto en un teléfono pequeño
- **THEN** el formulario se desplaza y el botón "Log in" sigue pudiéndose pulsar

#### Scenario: Contraseña oculta
- **WHEN** el usuario escribe en el campo de contraseña
- **THEN** los caracteres no se ven en pantalla

### Requirement: Cuándo se puede pulsar "Log in"
El botón "Log in" SHALL estar habilitado solo si el email y la contraseña tienen texto **después de recortar los espacios del inicio y del final**, y MUST estar deshabilitado mientras haya un login en curso.

#### Scenario: Campos vacíos
- **WHEN** el email y la contraseña están vacíos
- **THEN** "Log in" está deshabilitado

#### Scenario: Solo espacios
- **WHEN** el email es `"   "` y la contraseña tiene texto, o al revés
- **THEN** "Log in" está deshabilitado

#### Scenario: Ambos con texto
- **WHEN** el email y la contraseña tienen texto
- **THEN** "Log in" está habilitado

#### Scenario: Petición en curso
- **WHEN** el usuario pulsa "Log in" y el backend aún no ha respondido
- **THEN** "Log in" está deshabilitado y se ve el indicador de carga (`app_loading_indicator`) hasta que llega la respuesta

### Requirement: Inicio de sesión correcto
Con credenciales correctas de un usuario activo, la app SHALL iniciar la sesión con el token recibido y mostrar las pestañas principales, sin ningún mensaje de error. El email MUST enviarse sin los espacios del inicio y del final, y la contraseña tal como se escribió.

#### Scenario: Usuario activo
- **WHEN** el usuario entra con `standard@shoplab.dev` y `Secret_123`
- **THEN** la sesión queda iniciada, se muestran las pestañas y no hay mensaje de error

#### Scenario: Email con mayúsculas y espacios
- **WHEN** el usuario entra con `  Standard@ShopLab.dev ` y `Secret_123`
- **THEN** la sesión queda iniciada

#### Scenario: Primera vez tras una expiración
- **WHEN** la sesión había expirado y el usuario entra con credenciales correctas
- **THEN** la sesión queda iniciada y la marca de expiración desaparece

### Requirement: Credenciales incorrectas
Si el backend responde `INVALID_CREDENTIALS`, la app SHALL mostrar en línea, bajo los campos, el texto "The email or password is incorrect." (`login_error_text`), sin iniciar sesión y sin abrir ninguna alerta. Los datos escritos MUST conservarse y el botón MUST volver a habilitarse.

#### Scenario: Contraseña incorrecta
- **WHEN** el usuario entra con `standard@shoplab.dev` y `Wrong_123`
- **THEN** aparece "The email or password is incorrect.", no hay sesión, el botón vuelve a estar habilitado y no se muestra el indicador de carga

#### Scenario: Cuenta bloqueada con contraseña incorrecta
- **WHEN** el usuario entra con `locked@shoplab.dev` y `Wrong_123`
- **THEN** aparece "The email or password is incorrect.", igual que con cualquier otra credencial incorrecta

### Requirement: Cuenta bloqueada
Si el backend responde `USER_LOCKED`, la app SHALL mostrar en línea el texto "Your account is locked. Please contact support." (`login_error_text`), sin iniciar sesión y sin abrir ninguna alerta.

#### Scenario: Usuario bloqueado con credenciales correctas
- **WHEN** el usuario entra con `locked@shoplab.dev` y `Secret_123`
- **THEN** aparece "Your account is locked. Please contact support." y no hay sesión

### Requirement: El mensaje de error se renueva en cada intento
El mensaje en línea SHALL desaparecer en cuanto el usuario pulsa "Log in" de nuevo, y MUST reemplazarse por el del nuevo resultado.

#### Scenario: Error y luego éxito
- **WHEN** hay un mensaje de credenciales incorrectas y el usuario corrige la contraseña y entra
- **THEN** el mensaje desaparece y se muestran las pestañas

#### Scenario: Un error reemplaza a otro
- **WHEN** hay un mensaje de credenciales incorrectas y el siguiente intento es con una cuenta bloqueada
- **THEN** se ve solo el mensaje de cuenta bloqueada

### Requirement: El mensaje desaparece al vaciar el email
El mensaje de error en línea SHALL desaparecer en cuanto el usuario deja el campo de email vacío (o solo con espacios). Mientras el email tenga texto, el mensaje MUST conservarse hasta el siguiente intento.

#### Scenario: Se borra el email
- **WHEN** hay un mensaje de error en línea y el usuario borra el contenido del campo de email
- **THEN** el mensaje desaparece

#### Scenario: Se edita el email sin vaciarlo
- **WHEN** hay un mensaje de error en línea y el usuario cambia una letra del email
- **THEN** el mensaje sigue visible

### Requirement: Otros errores con la alerta genérica
Si el login falla por cualquier otro motivo (sin conexión, error del servidor u otro error), la app SHALL mostrar la alerta genérica "Something went wrong" con el mensaje de usuario del error, sin mensaje en línea. "Try again" MUST repetir el login con los mismos datos y "Cancel" MUST volver al formulario con los datos escritos.

#### Scenario: Sin conexión
- **WHEN** el usuario pulsa "Log in" y el backend no es alcanzable
- **THEN** aparece la alerta "Something went wrong" con "Check your internet connection and try again.", y no aparece mensaje en línea

#### Scenario: "Try again"
- **WHEN** la alerta está visible y el usuario pulsa "Try again"
- **THEN** la app repite el login con el mismo email y contraseña

#### Scenario: "Cancel"
- **WHEN** la alerta está visible y el usuario pulsa "Cancel"
- **THEN** la alerta se cierra y el formulario conserva el email y la contraseña escritos

### Requirement: Error forzado desde el login (solo builds de depuración)
En los builds de depuración, un email con la forma `force<status>@<dominio>`, donde `<status>` es un número de 400 a 599 (por ejemplo `force503@shoplab.dev`), SHALL hacer que la app envíe en ese login la cabecera `X-Force-Error: <status>`, de modo que el backend responda ese error sin ejecutar el login. El resultado MUST ser la alerta genérica. La comparación MUST ignorar mayúsculas y espacios. En los builds de producción este comportamiento MUST NOT existir.

#### Scenario: Error 5xx forzado
- **WHEN** el usuario entra con `force503@shoplab.dev` y cualquier contraseña, con el backend en perfil `testing`
- **THEN** aparece la alerta "Something went wrong" con "Something went wrong on our side. Please try again later.", no hay mensaje en línea y no hay sesión

#### Scenario: Error 4xx forzado
- **WHEN** el usuario entra con `force418@shoplab.dev`
- **THEN** aparece la alerta "Something went wrong" con "Something went wrong. Please try again."

#### Scenario: "Try again" repite el error forzado
- **WHEN** la alerta de un error forzado está visible y el usuario pulsa "Try again"
- **THEN** la app repite el login y vuelve a aparecer la alerta

#### Scenario: Estado fuera de rango o texto que no coincide
- **WHEN** el email es `force200@shoplab.dev`, `force700@shoplab.dev`, `force@shoplab.dev` o `forcex@shoplab.dev`
- **THEN** se trata como un email normal y el backend responde `INVALID_CREDENTIALS`, con su mensaje en línea

### Requirement: Aviso de sesión expirada
Si el usuario llega al login porque la sesión expiró, la pantalla SHALL mostrar el aviso "Your session has expired. Please log in again." (`login_session_expired_text`) sobre los campos. El aviso MUST NOT mostrarse en el arranque normal, tras un logout ni tras un error por token ausente, y MUST desaparecer al iniciar sesión de nuevo.

#### Scenario: Sesión expirada
- **WHEN** la sesión expira estando en las pestañas
- **THEN** se vuelve al login y se ve el aviso "Your session has expired. Please log in again."

#### Scenario: Arranque normal
- **WHEN** se abre la app sin sesión previa
- **THEN** el login no muestra el aviso

#### Scenario: Sesión terminada sin expirar
- **WHEN** la sesión termina por logout o por token ausente
- **THEN** el login no muestra el aviso

### Requirement: Enlace a registro
La pantalla SHALL mostrar el enlace "Create account" (`login_register_link`). Mientras no exista la pantalla de registro, pulsarlo MUST NOT producir ningún efecto ni error.

#### Scenario: Pulsar el enlace
- **WHEN** el usuario pulsa "Create account"
- **THEN** la app sigue en el login sin cambios

### Requirement: Los datos no sobreviven al inicio de sesión
Al iniciar sesión con éxito, el formulario SHALL quedar vacío, de modo que tras un logout o una expiración el login arranque sin el email ni la contraseña anteriores.

#### Scenario: Logout tras un login
- **WHEN** el usuario inicia sesión y después la sesión termina
- **THEN** el login se muestra con el email y la contraseña vacíos

### Requirement: Apariencia del login
La pantalla de login SHALL usar colores semánticos del tema y verse y leerse bien en apariencia clara y oscura. El mensaje de error en línea y el aviso de sesión expirada MUST distinguirse del resto del texto.

#### Scenario: Modo oscuro
- **WHEN** el dispositivo está en modo oscuro y se muestra el login con un mensaje de error
- **THEN** todos los textos, campos y el mensaje son legibles
