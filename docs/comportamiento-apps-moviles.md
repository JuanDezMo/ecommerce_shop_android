# Comportamiento funcional de las apps móviles (iOS y Android)

Actualizado: 2026-10-14

Referencia común de **qué hace** cada pantalla de la app ShopLab y **cómo se comporta** ante cada respuesta del backend. Aplica igual a iOS y a Android; el **cómo se implementa** queda en el documento de arquitectura de cada plataforma.

| Documento | Para qué |
| --- | --- |
| [Guía de integración](guia-integracion-ios-android.md) | Contrato con el backend: endpoints, reglas, códigos de error, datos semilla |
| [Catálogo de IDs](catalogo-ids-accesibilidad.md) | IDs de accesibilidad y textos de alertas (contrato con Appium y Maestro) |
| Este documento | Comportamiento de la app: flujos, validaciones, mensajes, alertas y límites |

Ante una contradicción con el backend, manda el backend. Un texto o un ID nuevo se agrega primero al catálogo.

---

## 1. Reglas generales

| Tema | Comportamiento |
| --- | --- |
| Idioma de la UI | Inglés en pantallas, alertas y mensajes |
| Mensajes de error | Salen de una tabla por `code` del backend (§11). El `message` del backend nunca se muestra: solo va a los logs |
| Montos | Tipo decimal, nunca de coma flotante. Formato USD en inglés: `$59.99`, `$25.00` |
| Importes finales | Siempre los del backend (`unitPrice`, `subtotal`, `total`). La app solo calcula el **total estimado** del carrito |
| Fechas recibidas | `createdAt` y `termsAcceptedAt` en UTC, con 0 a 9 decimales |
| Fechas enviadas | Solo `birthDate`, como `yyyy-MM-dd` del calendario gregoriano, con la fecha que eligió el usuario, sin depender del idioma del dispositivo |
| Fechas mostradas | Formato fijo en inglés de EE. UU., en la zona del dispositivo: `Sep 15, 2026` |
| Opcionales vacíos | No se envían (`phone` vacío → se omite, nunca `""`) |
| Textos libres | Se recortan los espacios del inicio y del final antes de validar y de enviar |
| Funciones de fase 2 | No se muestran opciones que el backend no ofrece (editar perfil, recuperar contraseña, cancelar órdenes…) |

### 1.1 Países

Los desplegables de registro y checkout muestran solo países de América, con su nombre en inglés y en orden alfabético. Se envía el código.

| Nombre mostrado | Código enviado |
| --- | --- |
| Argentina | `AR` |
| Chile | `CL` |
| Colombia | `CO` |
| Mexico | `MX` |
| Peru | `PE` |
| United States | `US` |

El backend también acepta `ES` (España), pero la app no lo ofrece.

---

## 2. Sesión

| Evento | Comportamiento |
| --- | --- |
| Login correcto (200) | Se guarda el token y la app pasa a las pestañas |
| 401 `AUTH_TOKEN_EXPIRED`, `_INVALID` o `_MALFORMED` | Se borra el token y se vuelve al login con el aviso "Your session has expired. Please log in again." (`login_session_expired_text`). El carrito se conserva |
| 401 `AUTH_TOKEN_MISSING` | Es un bug de la app (no envió el token). Se vuelve al login **sin** el aviso de sesión expirada |
| Logout | Tras confirmar (§12), se borra el token **y se vacía el carrito** |
| Cerrar y abrir la app | El carrito se conserva. Que haya que volver a iniciar sesión depende de dónde guarde el token cada plataforma |

Los 401 se gestionan en un único punto de la capa de red, no en cada pantalla.

---

## 3. Navegación

- **Sin sesión:** Login y Registro.
- **Con sesión:** tres pestañas, **Catalog**, **Cart** (con badge del número total de unidades) y **Account**. Cada pestaña conserva su propia pila de navegación.

| Pantalla o presentación | Pestaña | Se llega desde |
| --- | --- | --- |
| Detalle de producto (navegación) | Catalog | Una fila del catálogo |
| Hoja "Add to cart" (hoja inferior) | Catalog | "Add to cart" en el detalle o en una fila |
| Checkout y Confirmación | Cart | "Checkout" y "Place order" |
| My orders | Account | "My orders" |
| Detalle de orden | Account o Cart | Una fila de "My orders", o "View order" en la confirmación |

---

## 4. Login

| Elemento | Comportamiento |
| --- | --- |
| Botón "Log in" | Habilitado solo si email y contraseña tienen texto **después de recortar los espacios**. Durante la petición se deshabilita |
| `INVALID_CREDENTIALS` | Mensaje en línea "The email or password is incorrect." (`login_error_text`) |
| `USER_LOCKED` | Mensaje en línea "Your account is locked. Please contact support." (`login_error_text`) |
| Red, 5xx u otros errores | Alerta genérica (§13) |
| Enlace a registro | `login_register_link` |

El login no usa alertas para credenciales o cuenta bloqueada: son errores de cada intento y en línea se verifican más rápido.

---

## 5. Registro

### 5.1 Cómo controla la app cada regla del backend

La meta es que un `VALIDATION_ERROR` del backend solo aparezca si la app y el backend se desalinean (un bug). Cada regla se controla de una de tres formas:

| Grupo | Cómo | Mensaje |
| --- | --- | --- |
| **A. El control no permite el error** | Lista cerrada, valor inicial o tipo de dato | No hace falta |
| **B. Validación local** | La misma regla del backend, antes de enviar | Error en línea |
| **C. Solo el backend lo decide** | Depende de datos del servidor | Mensaje por `code` |

| Campo | Control | Valor inicial | Grupo | Regla local y mensaje |
| --- | --- | --- | --- | --- |
| First name | Texto | Vacío | B | Obligatorio: "Enter your first name." · Se recortan los espacios de los extremos. Solo letras A-Z y a-z con un único espacio entre palabras (sin tildes ni números; dos espacios seguidos son un error y no se juntan solos): "First name can only contain letters (A-Z, a-z) and single spaces between words." · 10 a 30 caracteres contando los espacios: "First name must be 10 to 30 characters." Se muestra solo el primer error, en ese orden |
| Last name | Texto | Vacío | B | Igual que el nombre, con "last name" |
| Email | Texto, teclado de email | Vacío | B | "Enter your email." · Solo letras, números, `@` y puntos: "Email can only contain letters, numbers, @ and a dot." · Forma `texto@texto.texto` (un solo `@`, puntos solo en el dominio): "Enter a valid email address." · 8 a 20 caracteres contando el `@` y los puntos: "Email must be 8 to 20 characters." Primer error, en ese orden. Se envía recortado y en minúsculas |
| Email ya registrado | — | — | C | 409 `EMAIL_ALREADY_EXISTS` → "An account with this email already exists." en el campo email |
| Password | Texto oculto | Vacío | B | "Enter a password." · 10 a 20: "Password must be 10 to 20 characters." · Solo ASCII visible (sin espacios ni tildes), con mayúscula, minúscula, número y carácter especial (cualquiera que no sea letra ni dígito): "Use an uppercase letter, a lowercase letter, a number and a special character, without spaces." |
| Phone | Texto, teclado de teléfono | Vacío | B | **Obligatorio**: "Enter your phone number." · Formato `+` + 3 dígitos de prefijo + 10 dígitos (por ejemplo `+5733001234567`), sin espacios ni símbolos: "Use the format +CCC followed by 10 digits, for example +5733001234567." Se envía **sin el `+`** (el backend solo admite 7 a 15 dígitos) |
| Birth date | Tres menús como filas de una lista: Day, Month (nombres en inglés) y Year (del año actual a 1900). El día se ajusta al mes y al año (28 a 31; si deja de existir pasa al último del mes) | 1 / January / 2000 (un adulto: el formulario se puede enviar sin tocarlo, y las pruebas eligen otra fecha para provocar el error de edad) | A + B | El formato lo genera la app (A). Mayor de 18 años contando **con la fecha de hoy en UTC**, igual que el backend: "You must be at least 18 years old to register." |
| Country | Desplegable con los países de América (§1.1) | Sin selección | A + B | "Select your country." |
| Plan | Segmentado BASIC / PRO, etiquetas "Basic" y "Pro" | Basic | A | — |
| Experience level | Slider de 1 a 5, paso 1 | 3 | A | — |
| Newsletter | Switch | Apagado | A | — |
| Terms | Checkbox (estado legible: `checked` / `unchecked`) | Sin marcar | A + B | Se envía siempre como booleano. Sin marcar: "You must accept the terms and conditions." y no se envía |

**Lo que la app no hace:** recortar o bloquear lo que el usuario escribe (por ejemplo, impedir escribir 51 caracteres). Solo valida, así las suites pueden provocar todos los errores.

### 5.2 Cuándo se valida y cómo se muestra

El registro tiene **dos pantallas** ("Step 1 of 2" y "Step 2 of 2"): la primera, con nombre, apellido, email, contraseña y teléfono, termina en "Next" (`register_next_button`); la segunda, con fecha de nacimiento, país, plan, experiencia, newsletter y términos, termina en "Create account". La flecha atrás conserva lo escrito. Si tras enviar el backend rechaza un campo de la primera pantalla (por ejemplo, un email repetido), la app vuelve a ella.

1. Al pulsar "Next" se validan los campos de la primera pantalla y no se avanza si hay errores; al pulsar "Create account" se validan todos. Con errores, no se llama al backend.
2. Tras ese primer intento, cada campo se revalida al salir de él.
3. El error de un campo desaparece en cuanto el usuario lo edita.
4. Los botones "Next" y "Create account" están **siempre habilitados**.
5. Con errores, aparece el resumen arriba de la pantalla ("1 field needs attention" / "3 fields need attention", `register_error_summary_text`), que cuenta solo los campos de esa pantalla, y la pantalla se desplaza al primer campo con error.
6. Cada error va en rojo debajo de su control (`register_<campo>_error_text`) y solo existe mientras se ve.

### 5.3 Respuestas del backend

| Respuesta | Comportamiento |
| --- | --- |
| 201 | Alerta "Account created" (§12). "Log in" vuelve al login con el email ya escrito |
| 400 `VALIDATION_ERROR` | Red de seguridad: los `fieldErrors` sustituyen a los locales y se muestran tal como llegan, según §11.2 |
| 409 `EMAIL_ALREADY_EXISTS` | En el campo email |
| 422 `UNDERAGE_USER` | En el campo de fecha de nacimiento |
| 422 `TERMS_NOT_ACCEPTED` | En el checkbox de términos |
| Red, 5xx u otros | Alerta genérica (§13) |

---

## 6. Catálogo y detalle de producto

| Elemento | Comportamiento |
| --- | --- |
| Lista | `GET /products`, en el orden del backend. Se recarga **cada vez que la pantalla aparece** y con "deslizar para recargar" |
| Error al cargar | Alerta genérica. Si el usuario pulsa "Cancel", la lista muestra "We couldn't load the products. Pull to refresh." (`catalog_error_text`) |
| Producto agotado (`stock` 0) | Etiqueta "Sold out" sobre la imagen y botón "Add to cart" deshabilitado |
| Detalle | Navegación desde la fila. Muestra nombre, descripción, precio, imagen y stock como "10 in stock" / "1 in stock" (`product_detail_stock_text`). Con stock 0 se muestra "Sold out" en su lugar |
| Detalle con 404 `PRODUCT_NOT_FOUND` | Alerta genérica con el mensaje "This product is no longer available." |
| Imagen | Por `imageKey`, con un único mapeo `imageKey` → imagen por app. Clave desconocida → imagen de respaldo. Origen de las imágenes: siete ilustraciones planas en SVG, originales del proyecto (`product_<clave>` y `product_placeholder`), en `docs/imagenes-producto/` de iOS; Android las convierte a *vector drawable* para que las dos apps se vean igual |

### 6.1 Hoja "Add to cart"

- Se abre desde el detalle o desde una fila del catálogo.
- Selector de cantidad desde 1 hasta el menor de estos valores:
  - stock del producto menos lo que ya está en el carrito;
  - 20 menos el total de unidades del carrito (§7.1).
- Si el producto está agotado, o si sus unidades en el carrito ya igualan su stock, el botón "Add to cart" de la fila o del detalle se deshabilita.
- Si el carrito ya tiene 20 unidades, el botón sigue habilitado, pero al pulsarlo se muestra la alerta "Cart is full" (§12) en lugar de la hoja.
- Al confirmar, la hoja se cierra y aparece el snackbar "Added to cart" (`app_snackbar_text`, 3 s).

---

## 7. Carrito

### 7.1 Reglas

| Regla | Comportamiento |
| --- | --- |
| Dónde vive | Solo en la app (el backend no tiene carrito), guardado en el dispositivo |
| Una línea por producto | Añadir un producto que ya está suma a su línea (evita `DUPLICATE_ORDER_ITEM`) |
| **Máximo 20 unidades en total** | La suma de todas las líneas no pasa de 20. El backend permite 99 por ítem: el límite de la app es más estricto |
| Carrito lleno | Con 20 unidades, pulsar "Add to cart" muestra la alerta "Cart is full" (§12). En el carrito, el "+" de cada línea llega a su máximo y no sube más |
| Cantidad por línea | De 1 hasta el menor de: stock del producto, o lo que falte para llegar a 20 en total |
| Total estimado | Suma de precio del catálogo × cantidad. Es estimado: el backend cobra el precio vigente |
| Badge de la pestaña | Total de unidades |

### 7.2 Refresco del stock

Cada vez que el carrito aparece, y tras "Review cart" (§9), la app pide `GET /products` y:

1. ajusta cada cantidad al stock real;
2. quita las líneas de productos agotados o que ya no existen;
3. si algo cambió, muestra el snackbar "Your cart was updated to match available stock." (`app_snackbar_text`).

### 7.3 Ciclo de vida

| Evento | Carrito |
| --- | --- |
| Cerrar y abrir la app | Se conserva |
| Sesión expirada | Se conserva |
| Orden creada (201) | Se vacía |
| Logout | Se vacía |
| Cualquier error al comprar | Se conserva |

### 7.4 Acciones

| Acción | Comportamiento |
| --- | --- |
| Deslizar una línea | Botón "Delete" que quita la línea |
| Vaciar carrito | Hoja de acciones "Remove all items from your cart?" con "Clear cart" (destructivo) y "Cancel" |
| Carrito vacío | Estado vacío (`cart_empty_text`). El botón de checkout no existe |
| Checkout | Abre el formulario de checkout |

---

## 8. Checkout

### 8.1 Formulario

Mismo comportamiento de validación que el registro (§5.2), con su resumen `checkout_error_summary_text`.

| Campo | Control | Valor inicial | Grupo | Regla local y mensaje |
| --- | --- | --- | --- | --- |
| Address | Texto | Vacío | B | "Enter your address." · 5 a 120: "Address must be 5 to 120 characters." |
| City | Texto | Vacío | B | "Enter your city." · 2 a 60: "City must be 2 to 60 characters." |
| Postal code | Texto | Vacío | B | "Enter your postal code." · 3 a 10 letras, dígitos, espacios o guiones: "Use 3 to 10 letters, numbers, spaces or hyphens." |
| Country | Desplegable (misma lista del registro, §1.1) | Sin selección | A + B | "Select your country." |
| Payment method | Segmentado "Card" / "Cash" / "Transfer" | Card | A | — |
| Ítems | Salen del carrito | — | A | Nunca vacío, sin repetidos, cantidades válidas (§7.1) |

No se piden ni se envían datos de tarjeta.

### 8.2 Confirmar la compra

1. "Place order" (`checkout_confirm_button`) valida el formulario. Si hay errores, no sigue.
2. Abre la alerta "Place order?" con "Estimated total: $144.98." (el total estimado del carrito).
3. "Place order" envía `POST /orders`. "Cancel" vuelve al formulario.

### 8.3 Respuestas de `POST /orders`

| Respuesta | Comportamiento |
| --- | --- |
| 201 | Se vacía el carrito y se muestra la confirmación con los datos del backend |
| 409 `INSUFFICIENT_STOCK`, 404 `PRODUCT_NOT_FOUND`, 400 `EMPTY_ORDER` o `DUPLICATE_ORDER_ITEM` | Alerta "Can't place your order" con el mensaje de su `code`. "Review cart" vuelve al carrito, que refresca el stock (§7.2) |
| 400 `VALIDATION_ERROR` | Según §11.2 |
| 401 | Gestión global de la sesión (§2). El carrito se conserva |
| Red, 5xx u otros | Alerta genérica. **"Try again" vuelve a abrir "Place order?"**, no reenvía (§13) |

---

## 9. Confirmación

| Elemento | Comportamiento |
| --- | --- |
| Datos | Número de orden, líneas y total, todos de la respuesta 201 (sin otra consulta) |
| "View order" | Abre el detalle de esa orden |
| "Done" | Cierra el flujo de compra y **cambia a la pestaña Catalog**, que se recarga y muestra el stock actualizado |

---

## 10. Cuenta, mis órdenes y detalle de orden

### 10.1 Cuenta

| Elemento | Comportamiento |
| --- | --- |
| Perfil | `GET /users/me`: nombre completo, email y plan ("Basic" o "Pro") |
| Tema | Segmentado "System" / "Light" / "Dark", guardado en el dispositivo |
| "My orders" | Abre la lista de órdenes |
| Logout | Alerta "Log out?" (§12) |
| Error al cargar | Alerta genérica |

### 10.2 My orders

| Elemento | Comportamiento |
| --- | --- |
| Orden | La del backend: la más reciente primero |
| Fila | "Order #2" · `Sep 15, 2026` · número de unidades (suma de `quantity`: "1 item", "3 items") · total |
| Estado vacío | "You haven't placed any orders yet." |
| Recarga | Cada vez que la pantalla aparece y con "deslizar para recargar" |
| Error | Alerta genérica |

### 10.3 Detalle de orden

| Bloque | Comportamiento |
| --- | --- |
| Cabecera | "Order #1", estado "Confirmed", fecha y hora en la zona del dispositivo, pago "Card", "Cash" o "Transfer" |
| Productos | Imagen, nombre, `2 × $25.00` y subtotal, en el orden del backend |
| Total | El del backend (la app no suma) |
| Envío | Dirección, ciudad, código postal y país, en líneas separadas |
| 404 `ORDER_NOT_FOUND` | Alerta "Order not found". "OK" vuelve a la pantalla anterior, sea la lista o la confirmación |

---

## 11. Errores

### 11.1 Mensaje para el usuario por `code`

| `code` | Mensaje | Dónde |
| --- | --- | --- |
| `VALIDATION_ERROR` | "Some fields need your attention." + el error de cada campo | Resumen y campos |
| `INVALID_CREDENTIALS` | "The email or password is incorrect." | En línea, login |
| `USER_LOCKED` | "Your account is locked. Please contact support." | En línea, login |
| `EMAIL_ALREADY_EXISTS` | "An account with this email already exists." | Campo email |
| `UNDERAGE_USER` | "You must be at least 18 years old to register." | Campo fecha de nacimiento |
| `TERMS_NOT_ACCEPTED` | "You must accept the terms and conditions." | Checkbox de términos |
| `PRODUCT_NOT_FOUND` | "This product is no longer available." | Alerta genérica (detalle) o "Can't place your order" (checkout) |
| `INSUFFICIENT_STOCK` | "Some products in your cart don't have enough stock. Please update the quantities." | "Can't place your order" |
| `EMPTY_ORDER` | "Your cart is empty." | "Can't place your order" |
| `DUPLICATE_ORDER_ITEM` | "We couldn't process your cart. Please try again." | "Can't place your order" |
| `ORDER_NOT_FOUND` | "We couldn't find this order." | "Order not found" |
| `AUTH_TOKEN_EXPIRED`, `_INVALID`, `_MALFORMED` | "Your session has expired. Please log in again." | Aviso en el login |
| `AUTH_TOKEN_MISSING` | Sin aviso | Vuelve al login |
| Otro 4xx (incluido `FORCED_ERROR`) | "Something went wrong. Please try again." | Alerta genérica |
| 5xx (incluido `FORCED_ERROR`) | "Something went wrong on our side. Please try again later." | Alerta genérica |
| Sin conexión o tiempo agotado | "Check your internet connection and try again." | Alerta genérica |

### 11.2 `fieldErrors` del backend (red de seguridad)

Cada `field` del backend se muestra en su campo de la app:

| Formulario | `field` del backend → campo de la app |
| --- | --- |
| Registro | `firstName` → `firstname` · `lastName` → `lastname` · `email` → `email` · `password` → `password` · `phone` → `phone` · `birthDate` → `birthdate` · `country` → `country` · `plan` → `plan` · `experienceLevel` → `experience` · `termsAccepted` → `terms` |
| Checkout | `shipping.address` → `address` · `shipping.city` → `city` · `shipping.postalCode` → `postalcode` · `shipping.country` → `country` · `paymentMethod` → `payment` |
| Checkout, sin campo | `items`, `items[n]`, `items[n].productId`, `items[n].quantity`, `shipping` |

| Caso | Comportamiento |
| --- | --- |
| Un campo con varios mensajes | Todos, uno por línea, en el mismo `<pantalla>_<campo>_error_text` |
| Ningún error tiene campo en el formulario (incluido `Malformed JSON request` con `fieldErrors` vacío) | Registro: alerta genérica. Checkout: "Can't place your order" con "We couldn't process your cart. Please try again." |
| Errores con campo mezclados con otros sin campo | Los que tienen campo, en línea; además, la alerta del caso anterior. El resumen cuenta solo los campos visibles |

Los textos de `fieldErrors` son técnicos (`size must be between 2 and 50`) y se muestran tal como llegan: solo aparecen si una regla de la app se desalinea del backend.

---

## 12. Alertas, diálogos y mensajes

Los textos son contrato con las suites. Los IDs de los botones están en el catálogo.

| Pantalla | Momento | Tipo | Título · mensaje | Botones |
| --- | --- | --- | --- | --- |
| Registro | 201 | Alerta | "Account created" · "You can now log in with your email." | **Log in** |
| Detalle / catálogo | "Add to cart" | Hoja inferior | — | **Add to cart** |
| Detalle / catálogo | Producto añadido | Snackbar (3 s) | "Added to cart" | — |
| Detalle / catálogo | "Add to cart" con 20 unidades en el carrito | Alerta | "Cart is full" · "Your cart can hold up to 20 items." | **OK** |
| Carrito | Refresco con cambios | Snackbar (3 s) | "Your cart was updated to match available stock." | — |
| Carrito | Vaciar | Hoja de acciones | "Remove all items from your cart?" | **Clear cart** (destructivo) · **Cancel** |
| Carrito | Deslizar una línea | Acción de deslizar | — | **Delete** |
| Checkout | "Place order" | Alerta | "Place order?" · "Estimated total: $144.98." | **Place order** · **Cancel** |
| Checkout | 409, 404, `EMPTY_ORDER`, `DUPLICATE_ORDER_ITEM`, `fieldErrors` sin campo | Alerta | "Can't place your order" · mensaje de §11.1 | **Review cart** |
| Detalle de orden | 404 `ORDER_NOT_FOUND` | Alerta | "Order not found" · "We couldn't find this order." | **OK** |
| Cuenta | Logout | Alerta | "Log out?" · "Your cart will be emptied." | **Log out** (destructivo) · **Cancel** |
| Cualquiera | Red, 5xx, otros 4xx, `FORCED_ERROR` | Alerta genérica | "Something went wrong" · mensaje de §11.1 | **Try again** · **Cancel** |
| Login | Credenciales, cuenta bloqueada, sesión expirada | En línea | Mensajes de §11.1 | — |
| Registro / checkout | Errores de campo | En línea con resumen | §5 y §8 | — |

---

## 13. Alerta genérica: "Try again" y "Cancel"

| Dónde | "Try again" | "Cancel" |
| --- | --- | --- |
| Pantallas que consultan datos (catálogo, detalle, cuenta, mis órdenes, detalle de orden) | Repite la carga | Deja la pantalla vacía o con su mensaje de error |
| Login y registro | Reenvía el formulario. Es seguro: un registro ya creado responde `EMAIL_ALREADY_EXISTS` | Vuelve al formulario con los datos escritos |
| Checkout | **Vuelve a abrir "Place order?"** en lugar de reenviar | Vuelve al formulario |

**Por qué el checkout es distinto:** si la respuesta de `POST /orders` se pierde por la red, la orden pudo crearse en el backend. Reenviar sin preguntar podría crear una orden duplicada; con la confirmación, repetir la compra es una decisión consciente del usuario.

---

## 14. Formatos de visualización

| Dato | Formato |
| --- | --- |
| Monto | `$59.99`, `$25.00` |
| Fecha (listas) | `Sep 15, 2026` |
| Fecha y hora (detalle de orden) | `Sep 15, 2026, 11:30 AM`: fecha en el mismo formato más la hora (12 h, AM/PM), en la zona del dispositivo |
| Unidades | "1 item", "3 items" |
| Estado de orden | "Confirmed" |
| Forma de pago | "Card", "Cash", "Transfer" |
| Plan | "Basic", "Pro" |
| Número de orden | "Order #2" |

Las suites de UI verifican fechas, nunca horas: la hora depende de la zona del dispositivo.

---

## 15. Pruebas

| Tema | Comportamiento |
| --- | --- |
| Datos conocidos | `POST /api/v1/test/reset` antes de cada prueba de integración: vuelven los datos semilla de la guía |
| Sin mocks | Las pruebas de integración usan el backend real (perfil `testing`) |
| Carrito en pruebas | Las pruebas usan un almacenamiento propio, nunca el de la app |
| Provocar alertas | Ver la guía (`X-Force-Error`, `X-Delay`, producto 3, orden 999) |
| Inyectar cabeceras y token desde Maestro y Appium | Se define en la iteración final de pruebas móviles |

---

## 16. Pendiente de definir o de llevar al catálogo

| Tema | Estado |
| --- | --- |
| Textos e IDs nuevos | Agregar al catálogo: snackbar de carrito actualizado (§7.2), texto de error del catálogo (§6), alerta "Cart is full" con su botón `app_cart_full_alert_ok_button` (§12) |
| Notas del catálogo | Actualizar: cantidad máxima de la hoja (20 en total, no 99), `orders_item_<orderId>_count_text` = unidades, `order_detail_status_text` = "Confirmed" |
| Origen de las imágenes de producto | Iteración propia, antes del catálogo |
