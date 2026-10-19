# user-registration Specification

## Purpose
Define la pantalla de registro: qué datos pide, con qué valores iniciales y reglas de validación, qué se envía al backend, cómo se muestra cada respuesta y cómo se vuelve al login con la cuenta ya creada.

## Requirements

### Requirement: Acceso entre login y registro
Desde el login, "Create account" (`login_register_link`) SHALL abrir la pantalla de registro. Desde la primera pantalla del registro, el enlace al login (`register_login_link`) SHALL volver al login sin crear nada y sin perder lo que el usuario ya había escrito en el login.

#### Scenario: Ir al registro
- **WHEN** el usuario pulsa "Create account" en el login
- **THEN** se muestra la primera pantalla del registro ("Step 1 of 2") con sus campos en sus valores iniciales

#### Scenario: Volver al login
- **WHEN** el usuario pulsa el enlace al login en la primera pantalla del registro, o la flecha atrás del sistema
- **THEN** se vuelve al login y el email y la contraseña que había escrito allí siguen escritos

### Requirement: Registro en dos pantallas
El registro SHALL dividirse en dos pantallas consecutivas, con un indicador de paso ("Step 1 of 2" y "Step 2 of 2"). La primera pide los datos básicos y tiene el botón "Next" (`register_next_button`), siempre habilitado, que valida esa pantalla y avanza solo si no hay errores. La segunda tiene el resto de campos y el botón "Create account". Volver a la primera desde la segunda MUST conservar todo lo escrito.

#### Scenario: Primera pantalla
- **WHEN** se abre el registro
- **THEN** se ve "Step 1 of 2", los cinco campos básicos, el botón "Next" y el enlace al login, y no se ven la fecha, el país, el plan, la experiencia, la newsletter ni los términos

#### Scenario: Avanzar con datos válidos
- **WHEN** los cinco campos básicos son válidos y el usuario pulsa "Next"
- **THEN** se muestra la segunda pantalla ("Step 2 of 2") y no aparece ningún error

#### Scenario: Avanzar con errores
- **WHEN** hay errores en los campos básicos y el usuario pulsa "Next"
- **THEN** se queda en la primera pantalla, muestra los errores y no llama al backend

#### Scenario: Segunda pantalla
- **WHEN** se muestra la segunda pantalla
- **THEN** se ven la fecha, el país, el plan, la experiencia, la newsletter, los términos y el botón "Create account", y no se ven los campos básicos

#### Scenario: Volver a la primera pantalla
- **WHEN** el usuario vuelve atrás desde la segunda pantalla
- **THEN** la primera muestra lo que había escrito y la segunda conserva lo que había elegido al avanzar otra vez

### Requirement: Campos de cada pantalla
La primera pantalla SHALL tener nombre, apellido, email (teclado de email, sin mayúscula automática), contraseña (oculta) y teléfono (teclado de teléfono, obligatorio). La segunda SHALL tener la fecha de nacimiento (tres desplegables), el país (desplegable), el plan (segmentado Basic/Pro), el nivel de experiencia (slider de 1 a 5, paso 1, valor visible), la newsletter (interruptor) y los términos (casilla).

#### Scenario: Controles de la primera pantalla
- **WHEN** se muestra la primera pantalla
- **THEN** aparecen, en este orden, nombre, apellido, email, contraseña y teléfono

#### Scenario: Controles de la segunda pantalla
- **WHEN** se muestra la segunda pantalla
- **THEN** aparecen, en este orden, fecha de nacimiento, país, plan, experiencia, newsletter y términos

#### Scenario: Valor de la experiencia
- **WHEN** el usuario mueve el slider a 5
- **THEN** el valor visible cambia a 5 y el slider solo admite enteros de 1 a 5

#### Scenario: Estado de los términos
- **WHEN** el usuario marca la casilla de términos
- **THEN** su estado accesible (`stateDescription`) cambia de `unchecked` a `checked`

### Requirement: Valores iniciales
Al abrir el registro, los textos SHALL estar vacíos, la fecha de nacimiento en 1 / January / 2000, el país sin seleccionar ("Select"), el plan en Basic, la experiencia en 3, la newsletter apagada y los términos sin marcar. Cada visita al registro MUST empezar con estos valores.

#### Scenario: Formulario recién abierto
- **WHEN** se abre el registro y se avanza a la segunda pantalla
- **THEN** cada campo muestra su valor inicial

#### Scenario: Volver a abrir el registro
- **WHEN** el usuario escribe datos, vuelve al login y abre el registro otra vez
- **THEN** el formulario aparece con todos sus valores iniciales

### Requirement: Fecha de nacimiento en tres campos
La fecha de nacimiento SHALL elegirse con tres desplegables, Day (`register_birthdate_day_dropdown`), Month (`register_birthdate_month_dropdown`) y Year (`register_birthdate_year_dropdown`), que muestran sus opciones al pulsarlos. Month ofrece los nombres en inglés (January a December). Year ofrece del año actual a 1900, del más reciente al más antiguo. Day ofrece del 1 al último día del mes y año elegidos, y si al cambiar el mes o el año el día deja de existir, pasa al último día de ese mes.

#### Scenario: Opciones del mes
- **WHEN** el usuario pulsa Month
- **THEN** ve January, February, March... hasta December

#### Scenario: Opciones del año
- **WHEN** el usuario pulsa Year
- **THEN** la primera opción es el año actual y la última es 1900

#### Scenario: Días de febrero
- **WHEN** el mes es February y el año es 2023, o 2024
- **THEN** Day ofrece del 1 al 28, o del 1 al 29

#### Scenario: El día deja de existir
- **WHEN** el día es 31 y el usuario cambia el mes a February en el año 2000, o en 2001
- **THEN** el día pasa a 29, o a 28

#### Scenario: Fecha elegida
- **WHEN** el usuario elige 15, March y 1990
- **THEN** la fecha de nacimiento es el 15 de marzo de 1990

#### Scenario: Error bajo los tres campos
- **WHEN** la fecha elegida produce un error de edad
- **THEN** el error aparece una sola vez, bajo los tres desplegables, localizable por `register_birthdate_error_text`

### Requirement: Países disponibles
El desplegable de país SHALL ofrecer solo estos países, por su nombre en inglés y en orden alfabético: Argentina, Chile, Colombia, Mexico, Peru y United States. La app MUST enviar el código (`AR`, `CL`, `CO`, `MX`, `PE`, `US`) y MUST NOT ofrecer España.

#### Scenario: Lista de países
- **WHEN** el usuario abre el desplegable de país
- **THEN** ve, en este orden, Argentina, Chile, Colombia, Mexico, Peru y United States, además de la opción vacía "Select"

### Requirement: Validación de los nombres
El nombre y el apellido SHALL recortarse de espacios al inicio y al final antes de validarse. Tras el recorte MUST estar formados por letras de la A a la Z (mayúsculas o minúsculas), con un único espacio entre una palabra y la siguiente (sin tildes, números ni símbolos), y tener de 10 a 30 caracteres contando esos espacios. Se comprueba en este orden —vacío, caracteres, longitud— y se muestra solo el primer error.

#### Scenario: Nombre vacío o de espacios
- **WHEN** el nombre está vacío o solo tiene espacios y se pulsa "Next"
- **THEN** aparece "Enter your first name." (o "Enter your last name." en el apellido)

#### Scenario: Caracteres no permitidos
- **WHEN** el nombre es `Alexandri4`, `Fernándezzz` o `Ana-Maria Lopez`
- **THEN** aparece "First name can only contain letters (A-Z, a-z) and single spaces between words." (o "Last name can only contain letters (A-Z, a-z) and single spaces between words.")

#### Scenario: Más de un espacio seguido
- **WHEN** el nombre es `Ana  Maria Lopez`, con dos espacios entre las palabras
- **THEN** aparece el mismo mensaje de caracteres no permitidos, y la app no junta los espacios por su cuenta

#### Scenario: Longitud fuera de rango
- **WHEN** el nombre tiene 9 caracteres (`Alexandra` o `Ana Maria`) o 31
- **THEN** aparece "First name must be 10 to 30 characters." (o "Last name must be 10 to 30 characters.")

#### Scenario: Varios errores a la vez
- **WHEN** el nombre es `Ana1`, que es corto y tiene un número
- **THEN** aparece solo el mensaje de caracteres no permitidos

#### Scenario: Espacios al inicio y al final
- **WHEN** el nombre es `" ana maria lopez "`
- **THEN** no aparece error en ese campo y se envía `ana maria lopez`, sin los espacios de los extremos

#### Scenario: Nombre con un espacio entre palabras
- **WHEN** el nombre es `Ana Maria Lopez` (15 caracteres)
- **THEN** no aparece error en ese campo

#### Scenario: Nombre válido
- **WHEN** el nombre es `" Alexandria "` o tiene exactamente 10 o 30 caracteres válidos
- **THEN** no aparece error en ese campo

### Requirement: Validación del email
El email SHALL recortarse de espacios antes de validarse y MUST estar formado solo por letras, números, un único `@` y puntos en el dominio, con la forma `texto@texto.texto`, y tener de 8 a 20 caracteres contando el `@` y los puntos. Se comprueba en este orden: vacío, caracteres, formato y longitud.

#### Scenario: Email vacío
- **WHEN** el email está vacío y se pulsa "Next"
- **THEN** aparece "Enter your email."

#### Scenario: Caracteres no permitidos
- **WHEN** el email es `ana_123@x.com`, `ana-1@x.com`, `ana+1@x.com` o `ana 1@x.com`
- **THEN** aparece "Email can only contain letters, numbers, @ and a dot."

#### Scenario: Formato incorrecto
- **WHEN** el email es `anaxxxxxxx`, `ana@xxxxxx`, `ana@@x.com`, `@x.com` o `ana@.com`
- **THEN** aparece "Enter a valid email address."

#### Scenario: Longitud fuera de rango
- **WHEN** el email tiene 7 caracteres (`a@bc.de`) o 21 con formato válido
- **THEN** aparece "Email must be 8 to 20 characters."

#### Scenario: Email válido
- **WHEN** el email es `"  ANA@X.COM "`, `ana@example.com`, o tiene exactamente 8 caracteres (`ana@x.co`) o 20 (`abcdefghij@abcdef.co`)
- **THEN** no aparece error en ese campo

### Requirement: Validación de la contraseña
La contraseña MUST tener de 10 a 20 caracteres, todos ASCII visibles (sin espacios ni tildes), con al menos una minúscula, una mayúscula, un dígito y un carácter que no sea letra ni dígito. La contraseña MUST NOT recortarse. Se comprueba en este orden: vacía, longitud y composición.

#### Scenario: Contraseña vacía
- **WHEN** la contraseña está vacía y se pulsa "Next"
- **THEN** aparece "Enter a password."

#### Scenario: Longitud fuera de rango
- **WHEN** la contraseña tiene 9 caracteres, o 21
- **THEN** aparece "Password must be 10 to 20 characters."

#### Scenario: Composición incorrecta
- **WHEN** la contraseña no tiene mayúscula, o minúscula, o dígito, o carácter especial, o contiene un espacio o una `ñ`
- **THEN** aparece "Use an uppercase letter, a lowercase letter, a number and a special character, without spaces."

#### Scenario: Contraseña válida
- **WHEN** la contraseña es `Secret_123`, o tiene exactamente 10 o 20 caracteres válidos
- **THEN** no aparece error en ese campo

#### Scenario: Sin recorte
- **WHEN** la contraseña es `" Secret_123"`
- **THEN** aparece el error de composición, porque el espacio cuenta

### Requirement: Validación del teléfono
El teléfono es obligatorio y, una vez recortado, MUST tener el formato `+` seguido de 3 dígitos de prefijo y de 10 dígitos más (por ejemplo `+5733001234567`), sin espacios ni otros símbolos.

#### Scenario: Teléfono vacío
- **WHEN** el teléfono está vacío y se pulsa "Next"
- **THEN** aparece "Enter your phone number."

#### Scenario: Formato incorrecto
- **WHEN** el teléfono es `3001234567` (sin +), `+573300123456` (faltan dígitos), `+57330012345678` (sobran), `+57 3300 1234567` o `+57-3300123456`
- **THEN** aparece "Use the format +CCC followed by 10 digits, for example +5733001234567."

#### Scenario: Teléfono válido
- **WHEN** el teléfono es `+5733001234567` o `" +5733001234567 "`
- **THEN** no aparece error en ese campo

### Requirement: Disposición de la fecha de nacimiento
Los tres desplegables de la fecha SHALL mostrarse como las filas de una lista, una debajo de otra y dentro de una misma tarjeta, cada una con su etiqueta (Day, Month, Year) a la izquierda y su valor a la derecha, de modo que ocupen todo el ancho disponible sin recortar ningún valor.

#### Scenario: Filas de la lista
- **WHEN** se muestra la segunda pantalla
- **THEN** Day, Month y Year aparecen en tres filas, una debajo de otra, con el nombre del mes completo visible aunque sea "September"

### Requirement: Mayoría de edad
El usuario MUST tener 18 años cumplidos. La app SHALL tomar año, mes y día de la fecha elegida y compararlos con la fecha de hoy en UTC, igual que el backend. Si no los cumple, aparece "You must be at least 18 years old to register." bajo la fecha.

#### Scenario: Cumple 18 años hoy
- **WHEN** hoy es 2026-10-11 (UTC) y la fecha elegida es 2008-10-11
- **THEN** no aparece error en ese campo

#### Scenario: Cumple 18 años mañana
- **WHEN** hoy es 2026-10-11 (UTC) y la fecha elegida es 2008-10-12
- **THEN** aparece "You must be at least 18 years old to register."

#### Scenario: Nacido un 29 de febrero
- **WHEN** hoy es 2026-02-28 (UTC) y la fecha elegida es 2008-02-29
- **THEN** aparece "You must be at least 18 years old to register."

#### Scenario: Valor inicial
- **WHEN** el usuario no toca la fecha
- **THEN** 2000-01-01 es una fecha válida y no produce error

### Requirement: País y términos obligatorios
El país MUST estar seleccionado ("Select your country.") y la casilla de términos MUST estar marcada ("You must accept the terms and conditions.").

#### Scenario: País sin elegir
- **WHEN** no hay país seleccionado y se pulsa "Create account"
- **THEN** aparece "Select your country."

#### Scenario: Términos sin marcar
- **WHEN** la casilla está sin marcar y se pulsa "Create account"
- **THEN** aparece "You must accept the terms and conditions." y no se envía la petición

### Requirement: Datos que se envían
Al registrar, la app SHALL enviar los textos sin los espacios de los extremos (el nombre y el apellido conservan el espacio entre palabras), el email en minúsculas, la contraseña tal como se escribió, el teléfono sin el `+` (solo sus 13 dígitos, porque el backend solo acepta dígitos) y la fecha de nacimiento como texto `yyyy-MM-dd` con el año, mes y día elegidos, sin depender del idioma del dispositivo. País, plan, nivel, newsletter y términos van con sus valores del backend.

#### Scenario: Fecha elegida
- **WHEN** el usuario deja la fecha en 1 / January / 2000
- **THEN** se envía `"2000-01-01"`, aunque el dispositivo use otra zona horaria o idioma

#### Scenario: Teléfono sin el signo más
- **WHEN** el teléfono escrito es `+5733001234567`
- **THEN** se envía `5733001234567`

#### Scenario: Email normalizado
- **WHEN** el email es `"  Ana@X.COM "`
- **THEN** se envía `ana@x.com`

### Requirement: Cuenta creada
Si el backend responde 201, la app SHALL mostrar la alerta "Account created" con el mensaje "You can now log in with your email." y un único botón "Log in" (`register_success_alert_login_button`). Al pulsarlo MUST volver al login con el email registrado ya escrito y la contraseña vacía. La app MUST NOT iniciar sesión sola.

#### Scenario: Registro correcto
- **WHEN** el usuario envía un formulario válido con un email nuevo
- **THEN** aparece la alerta "Account created" · "You can now log in with your email."

#### Scenario: Volver al login con el email
- **WHEN** el usuario pulsa "Log in" en la alerta
- **THEN** se muestra el login con el email registrado escrito, la contraseña vacía y sin sesión iniciada

#### Scenario: Login con la cuenta nueva
- **WHEN** el usuario entra con el email y la contraseña recién registrados
- **THEN** inicia sesión y ve las pestañas

### Requirement: Errores de negocio en su campo
Si el backend responde `EMAIL_ALREADY_EXISTS`, la app SHALL mostrar "An account with this email already exists." bajo el email. Con `UNDERAGE_USER`, "You must be at least 18 years old to register." bajo la fecha. Con `TERMS_NOT_ACCEPTED`, "You must accept the terms and conditions." bajo los términos. En los tres casos MUST NOT abrirse ninguna alerta.

#### Scenario: Email ya registrado
- **WHEN** el usuario se registra con `standard@shoplab.dev`
- **THEN** aparece "An account with this email already exists." bajo el email, el resumen cuenta 1 campo y no hay alerta

#### Scenario: Mismo email con otras mayúsculas
- **WHEN** el usuario se registra con `Standard@ShopLab.dev`
- **THEN** aparece el mismo mensaje bajo el email

#### Scenario: Error de la primera pantalla desde la segunda
- **WHEN** el usuario envía desde la segunda pantalla un email ya registrado
- **THEN** la app vuelve a la primera pantalla y muestra el error bajo el email

#### Scenario: Error de la segunda pantalla
- **WHEN** el backend rechaza un campo de la segunda pantalla, como los términos
- **THEN** la app se queda en la segunda pantalla y muestra el error bajo ese campo

### Requirement: Otros errores con la alerta genérica
Si el registro falla por red, error del servidor u otro error sin campo, la app SHALL mostrar la alerta genérica "Something went wrong". "Try again" MUST reenviar el formulario (es seguro: un registro ya creado responde "email ya registrado") y "Cancel" MUST volver al formulario con los datos escritos.

#### Scenario: Sin conexión
- **WHEN** el usuario envía un formulario válido y el backend no es alcanzable
- **THEN** aparece la alerta "Something went wrong" con "Check your internet connection and try again."

#### Scenario: "Try again"
- **WHEN** la alerta está visible y el usuario pulsa "Try again"
- **THEN** la app reenvía el mismo formulario

#### Scenario: "Cancel"
- **WHEN** la alerta está visible y el usuario pulsa "Cancel"
- **THEN** la alerta se cierra y el formulario conserva todos los datos escritos

### Requirement: Un solo envío a la vez
Mientras una petición de registro está en curso, la app SHALL mostrar el indicador de carga (`app_loading_indicator`) y MUST NOT enviar otra petición aunque el usuario vuelva a pulsar "Create account". Al terminar, el indicador desaparece.

#### Scenario: Doble pulsación
- **WHEN** el usuario pulsa "Create account" dos veces seguidas con un formulario válido
- **THEN** solo se crea una cuenta y no aparece un error de email repetido

### Requirement: Error forzado desde el registro (solo builds de depuración)
En los builds de depuración, un email con la forma `force<status>@<dominio>` (por ejemplo `force503@shoplab.dev`), con `<status>` entre 400 y 599, SHALL hacer que la app envíe `X-Force-Error: <status>` en ese registro, y el resultado MUST ser la alerta genérica. En producción este comportamiento MUST NOT existir. El formulario MUST pasar antes la validación local.

#### Scenario: Error 503 forzado
- **WHEN** el usuario envía un formulario válido con el email `force503@shoplab.dev`, con el backend en perfil `testing`
- **THEN** aparece la alerta "Something went wrong" con "Something went wrong on our side. Please try again later.", y no se crea ninguna cuenta

### Requirement: Apariencia del registro
El registro SHALL usar colores semánticos del tema y los mismos estilos de campo y de aviso que el login, y MUST ser legible en apariencia clara y oscura y usable con el teclado abierto en un teléfono pequeño.

#### Scenario: Modo oscuro
- **WHEN** el dispositivo está en modo oscuro y el formulario muestra errores
- **THEN** los campos, los textos y los errores en rojo son legibles
