# checkout Specification

## Purpose
Define cómo el usuario compra lo que tiene en el carrito: el formulario de envío y pago, sus reglas de validación, la confirmación previa y cómo se resuelve cada respuesta del backend sin perder el carrito.

## Requirements

### Requirement: Formulario de checkout
La pantalla de checkout SHALL pedir la dirección, la ciudad, el código postal, el país (menú desplegable con los países de la app, sin selección inicial) y la forma de pago (Card, Cash o Transfer, con Card elegida al inicio), y SHALL mostrar el total estimado del carrito y el botón "Place order". La app MUST NOT pedir ni enviar datos de tarjeta.

#### Scenario: Valores iniciales
- **WHEN** el usuario abre el checkout con 3 unidades en el carrito
- **THEN** los campos de texto están vacíos, el país sin seleccionar, Card elegida y el total estimado visible

### Requirement: Reglas de los campos
Tras recortar los espacios de los extremos, la dirección SHALL tener de 5 a 120 caracteres, la ciudad de 2 a 60, y el código postal de 3 a 10 caracteres entre letras, dígitos, espacios y guiones. El país SHALL ser obligatorio. Los mensajes SHALL ser: "Enter your address." / "Address must be 5 to 120 characters.", "Enter your city." / "City must be 2 to 60 characters.", "Enter your postal code." / "Use 3 to 10 letters, numbers, spaces or hyphens." y "Select your country.".

#### Scenario: Límites de la dirección
- **WHEN** la dirección tiene 4, 5, 120 y 121 caracteres
- **THEN** 4 y 121 dan "Address must be 5 to 120 characters." y 5 y 120 son válidas

#### Scenario: Código postal
- **WHEN** el código postal es `SW1A 1AA`, `12`, `12#45` o `12345678901`
- **THEN** solo `SW1A 1AA` es válido y los demás dan "Use 3 to 10 letters, numbers, spaces or hyphens."

#### Scenario: Solo espacios
- **WHEN** la dirección son solo espacios
- **THEN** se muestra "Enter your address."

### Requirement: Validación y errores del checkout
Al pulsar "Place order", la app SHALL validar todos los campos y, si hay errores, MUST NOT abrir la confirmación ni llamar al backend. El botón MUST estar siempre habilitado. Los errores, el resumen de campos, el desplazamiento al primero, la revalidación al salir de un campo y la desaparición del error al editarlo SHALL comportarse como en el resto de formularios de la app.

#### Scenario: Formulario vacío
- **WHEN** el usuario pulsa "Place order" sin rellenar nada
- **THEN** ve 4 errores (dirección, ciudad, código postal y país), el resumen "4 fields need attention", y no se abre ninguna alerta

### Requirement: Confirmar la compra
Con el formulario válido, "Place order" SHALL abrir la alerta "Place order?" con el texto "Estimated total: $X." (el total estimado del carrito) y los botones "Place order" y "Cancel". Solo el botón "Place order" de la alerta SHALL enviar la orden, y mientras la petición está en curso MUST NOT poder enviarse otra; "Cancel" SHALL volver al formulario sin enviar nada.

#### Scenario: Cancelar
- **WHEN** el usuario pulsa "Place order" con datos válidos y luego "Cancel" en la alerta
- **THEN** vuelve al formulario con lo escrito y no se envía ninguna orden

### Requirement: Compra exitosa
Cuando el backend crea la orden (201), la app SHALL vaciar el carrito y mostrar la confirmación con los datos de la respuesta. Se SHALL enviar el producto y la cantidad de cada línea del carrito, la forma de pago y los datos de envío sin espacios en los extremos.

#### Scenario: Dos productos
- **WHEN** el carrito tiene 2 unidades del producto 1 y 1 del producto 4 y el usuario confirma con datos válidos
- **THEN** aparece la confirmación con total `$144.98`, y el carrito y la insignia quedan vacíos

### Requirement: Compra rechazada por el negocio
Si el backend rechaza la orden por `INSUFFICIENT_STOCK`, `PRODUCT_NOT_FOUND`, `EMPTY_ORDER` o `DUPLICATE_ORDER_ITEM`, la app SHALL mostrar la alerta "Can't place your order" con el mensaje de ese código y el botón "Review cart", que vuelve al carrito (que se ajusta al stock real al aparecer). El carrito MUST NOT modificarse.

#### Scenario: Producto agotado
- **WHEN** el carrito guardado tiene una unidad del producto 3 (agotado) y el usuario confirma
- **THEN** aparece "Can't place your order" con el mensaje de stock insuficiente y el carrito conserva su línea

#### Scenario: Última unidad comprada
- **WHEN** el usuario compra la última unidad del producto 5 y luego intenta comprarla otra vez
- **THEN** la segunda compra muestra "Can't place your order"

#### Scenario: Review cart
- **WHEN** el usuario pulsa "Review cart"
- **THEN** vuelve a la pantalla del carrito, que se ajusta al stock real

### Requirement: Errores de campo del backend
Si el backend responde `VALIDATION_ERROR` con errores de campo del checkout, la app SHALL mostrarlos en su campo tal como llegan, sustituyendo a los locales. Si algún error no corresponde a un campo del checkout (o no hay errores de campo), SHALL mostrar "Can't place your order" con "We couldn't process your cart. Please try again.". El carrito MUST NOT modificarse.

#### Scenario: Campo conocido
- **WHEN** el backend responde `VALIDATION_ERROR` con un error de la ciudad
- **THEN** el mensaje aparece bajo el campo de ciudad

#### Scenario: Error de ítems
- **WHEN** el backend responde `VALIDATION_ERROR` solo con un error de un ítem
- **THEN** aparece "Can't place your order" con "We couldn't process your cart. Please try again."

### Requirement: Otros errores al comprar
Ante un error de red, 5xx u otro no previsto, la app SHALL mostrar la alerta genérica y el carrito MUST NOT modificarse. "Try again" SHALL volver a abrir "Place order?" y MUST NOT reenviar la orden por sí mismo.

#### Scenario: Backend caído
- **WHEN** el usuario confirma y el backend no responde
- **THEN** aparece la alerta genérica y, al pulsar "Try again", se vuelve a abrir "Place order?" sin enviar nada

### Requirement: Teclado y desplazamiento
El formulario SHALL poder usarse con el teclado abierto: cada campo de texto que recibe el foco, y el primer campo con error tras un envío, MUST quedar visible, y el botón "Place order" MUST poder alcanzarse.

#### Scenario: Campo bajo el teclado
- **WHEN** el usuario toca el código postal con el teclado cerrado en un teléfono pequeño
- **THEN** el campo queda visible sobre el teclado

#### Scenario: Botón alcanzable
- **WHEN** el usuario terminó de escribir el código postal
- **THEN** puede desplazarse o cerrar el teclado y pulsar "Place order"
