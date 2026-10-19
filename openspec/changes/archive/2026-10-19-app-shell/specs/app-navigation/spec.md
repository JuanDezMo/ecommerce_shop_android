# Spec Delta

## Purpose
Define qué pantalla ve el usuario al abrir la app según tenga sesión o no, y la barra de pestañas principal (Catalog, Cart y Account) con sus etiquetas e identificadores de accesibilidad.

## ADDED Requirements

### Requirement: Pantalla inicial según la sesión
La app SHALL mostrar el flujo de autenticación cuando no hay sesión iniciada y las pestañas principales cuando la hay. El cambio MUST ocurrir en cuanto cambia el estado de la sesión, sin que el usuario haga nada más.

#### Scenario: Arranque sin sesión
- **WHEN** se abre la app y no hay sesión iniciada
- **THEN** se muestra el flujo de autenticación y no se muestra la barra de pestañas

#### Scenario: Sesión iniciada
- **WHEN** la sesión pasa a estar iniciada
- **THEN** se muestran las pestañas principales en lugar del flujo de autenticación

#### Scenario: Sesión terminada o expirada
- **WHEN** la sesión termina o expira estando en las pestañas principales
- **THEN** se vuelve al flujo de autenticación

### Requirement: Pantalla provisional de login
Mientras no exista la pantalla real de login, el flujo de autenticación SHALL mostrar una pantalla provisional con el título "Login" dentro de una barra superior.

#### Scenario: Flujo de autenticación visible
- **WHEN** se muestra el flujo de autenticación
- **THEN** aparece la pantalla provisional con el título "Login" y, en debug, el botón de sesión de prueba

### Requirement: Pestañas principales
Las pestañas principales SHALL ser tres, en este orden: "Catalog", "Cart" y "Account". Cada una MUST tener un icono y el identificador de accesibilidad `tab_catalog`, `tab_cart` o `tab_account`, respectivamente. Cada pestaña SHALL mostrar un contenido provisional hasta que su iteración lo sustituya.

#### Scenario: Tres pestañas
- **WHEN** se muestran las pestañas principales
- **THEN** aparecen "Catalog", "Cart" y "Account", en ese orden, cada una con su icono

#### Scenario: Identificadores de las pestañas
- **WHEN** se inspecciona la barra de pestañas con `maestro hierarchy` o Appium Inspector
- **THEN** cada elemento de la barra inferior tiene como `resource-id` `tab_catalog`, `tab_cart` o `tab_account`, o, si el sistema no propaga el identificador, se localiza por su etiqueta

#### Scenario: Pestaña inicial
- **WHEN** se muestran las pestañas por primera vez
- **THEN** la pestaña seleccionada es "Catalog"

### Requirement: Pestaña seleccionada controlable por la app
La pestaña seleccionada SHALL poder cambiarse desde cualquier pantalla de la app, no solo tocando la barra, y el cambio MUST reflejarse en la barra.

#### Scenario: El usuario toca una pestaña
- **WHEN** el usuario toca la pestaña "Cart"
- **THEN** se muestra el contenido de "Cart" y queda seleccionada

#### Scenario: La app cambia la pestaña
- **WHEN** una pantalla pide seleccionar la pestaña "Catalog"
- **THEN** la barra muestra "Catalog" como seleccionada y se ve su contenido

### Requirement: Apariencia de la estructura
La raíz y las pestañas SHALL usar colores semánticos del tema (`MaterialTheme.colorScheme`) y verse bien en claro y oscuro, sin colores fijos.

#### Scenario: Modo oscuro
- **WHEN** el dispositivo está en modo oscuro y se muestran las pestañas o el flujo de autenticación
- **THEN** los fondos y el texto cambian a su variante oscura y el texto es legible
