# Diseño: mobile-testing

## Contexto

Tras `account-orders`: `TestTagsTest` comprueba app → catálogo; `proguard-rules.pro` solo tiene un comentario y `release` ya tiene `isMinifyEnabled = true`; el permiso `ACCESS_LOCAL_NETWORK` está en el manifiesto debug. En `../ecommerce_shop_ios/maestro/` hay `config.yaml`, `run.sh`, `flows/{00,01,02,03,06}` y `subflows/{login,add-to-cart}` con el `appId` de iOS.

## Decisiones

### 1. IDs del catálogo en las dos direcciones

`TestTagsTest` extrae del catálogo todo texto entre comillas invertidas con forma de ID en la primera columna de una tabla. Las plantillas (`<productId>`, `<imageKey>`, `<orderId>`) se comparan con las funciones dinámicas de `TestTags` (se amplía la lista que ya existe). Lo que sea del catálogo y no esté en la app va a una lista de excepciones del test con su motivo; el objetivo es que quede vacía o casi. Los IDs `-Increment`/`-Decrement` los genera `QuantityStepper`.

### 2. Maestro: carpeta propia en Android

Se copia `maestro/` de iOS a este repositorio y se cambia el `appId` a `co.com.learningswift.shop`. Se acepta la duplicación (decisión del responsable, sustituye a la del plan "una sola carpeta"); si un flujo cambia por comportamiento, se cambia en las dos y se anota en la guía. `run.sh <serial> [flujo...]`: `reset` con `curl`, `adb shell pm grant … ACCESS_LOCAL_NETWORK` y `maestro --device <serial> test`. Ajustes de Android solo donde fallen: `hideKeyboard`, casilla con `checked: true`, desplegable de país, alertas por ID.

### 3. R8

Reglas mínimas para `kotlinx.serialization` (`@Serializable`, `$$serializer`, `Companion`), Ktor/OkHttp (`-dontwarn` de lo opcional) y los DTO. El criterio es que el APK **se ejecute**: login → catálogo → carrito → compra → "My orders". Release apunta a una URL de producción que no existe, así que para la prueba se usa una configuración temporal con la URL del emulador y la firma de debug, que no se commitea.

## Riesgos

| Riesgo | Mitigación |
| --- | --- |
| R8 quita una clase que usa reflexión | Se ejecuta el APK; cada fallo se convierte en una regla con su comentario |
| Un flujo de iOS usa un gesto que no existe en Android | Se ajusta en el flujo de Android y se anota en la guía |
| Un ID del catálogo no existe en Android | Se añade a la app o se lista como excepción |
