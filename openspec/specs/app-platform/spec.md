# app-platform Specification

## Purpose
Define en qué dispositivos y sistemas corre la app ShopLab Android, cómo se adapta a la apariencia clara u oscura y cómo llega al backend de desarrollo en la red local.

## Requirements

### Requirement: Versión mínima del sistema
La app SHALL funcionar en Android 8.0 (API 26) o superior, y en emuladores de API 34 y de la versión actual del sistema.

#### Scenario: Emulador API 34
- **WHEN** la app se ejecuta en un emulador de API 34
- **THEN** compila, abre y muestra su primera pantalla

#### Scenario: Emulador actual
- **WHEN** la app se ejecuta en un emulador con la versión actual de Android
- **THEN** compila, abre y muestra su primera pantalla

### Requirement: Apariencia clara y oscura
La app SHALL seguir la apariencia del sistema (clara u oscura) y MUST mantener el texto legible en ambas. El color de marca SHALL tener una variante para cada apariencia (`#2F5BDB` clara y `#7A9BFF` oscura) y la app MUST NOT usar colores de Material You dinámicos.

#### Scenario: Sistema en modo oscuro
- **WHEN** el dispositivo está en modo oscuro y se abre la app
- **THEN** la app usa fondos oscuros, texto claro y la variante oscura del color de marca

#### Scenario: Cambio de apariencia con la app abierta
- **WHEN** el usuario cambia el sistema de claro a oscuro con la app abierta
- **THEN** la app cambia de apariencia sin reiniciarse

### Requirement: Acceso al backend de desarrollo
La variante debug SHALL poder conectarse por HTTP sin TLS a `10.0.2.2`, `localhost` y al nombre `.local` de la máquina de desarrollo. La variante release MUST NOT permitir tráfico sin TLS.

#### Scenario: Emulador contra el backend local
- **WHEN** la app debug corre en el emulador con el backend levantado en la máquina de desarrollo
- **THEN** puede llamar a `http://10.0.2.2:8080/api/v1/` sin errores de seguridad de transporte

#### Scenario: Release sin tráfico en claro
- **WHEN** se revisa el manifiesto de la variante release
- **THEN** no declara ninguna excepción de tráfico sin TLS

### Requirement: Datos locales fuera de las copias de seguridad
La app SHALL excluir sus datos locales de las copias de seguridad del sistema y de la transferencia entre dispositivos.

#### Scenario: Manifiesto
- **WHEN** se revisa `AndroidManifest.xml`
- **THEN** `allowBackup` es `false` y hay reglas de extracción de datos que excluyen todo

### Requirement: Almacenamiento local cifrado
La app SHALL guardar los datos locales a través de la interfaz `KeyValueStore`, con una implementación en memoria para los tests y otra cifrada con Tink (AES-256-GCM) cuya clave vive en Android Keystore. MUST NOT usar `EncryptedSharedPreferences`, por estar obsoleta.

#### Scenario: Valor guardado y leído
- **WHEN** se guarda un valor con el almacén cifrado y se lee con la misma clave
- **THEN** se obtiene el mismo valor, y el texto guardado en disco no contiene el valor en claro

#### Scenario: Cifrado en un dispositivo
- **WHEN** corre el test instrumentado de `AndroidKeystoreAead` en un emulador
- **THEN** cifrar y descifrar devuelve el texto original
