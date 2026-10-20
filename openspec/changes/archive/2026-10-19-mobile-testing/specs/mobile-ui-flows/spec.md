## ADDED Requirements

### Requirement: Carpeta de flujos propia de Android
El repositorio SHALL tener una carpeta `maestro/` con los flujos, subflujos y un `run.sh` para Android, con el `appId` de la app. Un cambio de comportamiento que obligue a cambiar un flujo SHALL anotarse en la guía de Maestro para que se replique en la carpeta de iOS.

#### Scenario: Ejecutar un flujo
- **WHEN** se ejecuta `maestro/run.sh emulator-5554 flows/00-humo.yaml`
- **THEN** Maestro lanza la app de Android y el flujo pasa

### Requirement: Preparación del entorno por flujo
El script de ejecución SHALL dejar el backend en su estado semilla (`POST /api/v1/test/reset`) antes de cada flujo y SHALL conceder el permiso `ACCESS_LOCAL_NETWORK` a la app antes de lanzarla.

#### Scenario: Flujo repetido
- **WHEN** `02-registro` se ejecuta dos veces seguidas con `run.sh`
- **THEN** pasa las dos, porque el email ya no existe tras el `reset`

#### Scenario: Sin diálogo del sistema
- **WHEN** un flujo lanza la app en Android 17
- **THEN** el diálogo de red local no aparece y la primera pantalla es la de la app

### Requirement: Flujos que pasan en Android
Los flujos `00-humo`, `01-login`, `02-registro`, `03-catalogo-y-carrito-lleno` y `06-checkout-exito` SHALL pasar en el emulador de la versión actual de Android, en tema claro y oscuro.

#### Scenario: Tema claro
- **WHEN** se ejecutan los cinco flujos con el emulador en modo claro
- **THEN** los cinco pasan

#### Scenario: Tema oscuro
- **WHEN** se ejecutan con el emulador en modo oscuro
- **THEN** los cinco pasan
