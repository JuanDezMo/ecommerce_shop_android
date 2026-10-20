# app-appearance Specification

## Purpose
Define el tema que el usuario elige para la app (System, Light o Dark): cómo se aplica, cuándo cambia y que se conserva entre aperturas.

## Requirements

### Requirement: Tema elegido por el usuario
La app SHALL ofrecer tres temas, "System", "Light" y "Dark", con "System" como valor inicial. "System" SHALL seguir la apariencia del dispositivo; "Light" y "Dark" SHALL fijar esa apariencia en toda la app, también en el login y el registro, sin importar la del sistema.

#### Scenario: Valor inicial
- **WHEN** el usuario abre la app por primera vez
- **THEN** el tema elegido es "System"

#### Scenario: Fijar el tema oscuro
- **WHEN** el sistema está en modo claro y el usuario elige "Dark"
- **THEN** toda la app se ve en modo oscuro

### Requirement: El tema cambia al instante
Al elegir un tema, la app SHALL cambiar de apariencia en ese momento, sin reiniciarse ni salir de la pantalla.

#### Scenario: Cambio en vivo
- **WHEN** el usuario está en la pestaña Account y elige "Light" estando en modo oscuro
- **THEN** la pantalla cambia a modo claro de inmediato

### Requirement: El tema se conserva
El tema elegido SHALL guardarse en el dispositivo y mantenerse al cerrar y volver a abrir la app, y al cerrar la sesión.

#### Scenario: Reabrir la app
- **WHEN** el usuario elige "Dark", cierra la app y la vuelve a abrir
- **THEN** la app se abre en modo oscuro y el selector muestra "Dark"

#### Scenario: Cerrar sesión
- **WHEN** el usuario elige "Dark" y cierra sesión
- **THEN** el login se ve en modo oscuro
