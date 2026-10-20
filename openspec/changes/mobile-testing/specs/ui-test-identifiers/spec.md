## ADDED Requirements

### Requirement: IDs de la app y del catálogo coinciden
Todo ID declarado en `TestTags` SHALL existir en `docs/catalogo-ids-accesibilidad.md`, y todo ID del catálogo SHALL existir en la app, ya sea como constante o como plantilla dinámica. Un ID del catálogo que solo exista en iOS SHALL figurar en una lista de excepciones de la prueba, con su motivo.

#### Scenario: ID de la app sin catálogo
- **WHEN** `TestTags` declara un ID que no está en el catálogo
- **THEN** `TestTagsTest` falla indicando el ID

#### Scenario: ID del catálogo sin app
- **WHEN** el catálogo tiene un ID que ni `TestTags` ni la lista de excepciones contienen
- **THEN** `TestTagsTest` falla indicando el ID

#### Scenario: Plantilla dinámica
- **WHEN** el catálogo define `catalog_item_<productId>_add_button`
- **THEN** la función de `TestTags` correspondiente devuelve `catalog_item_4_add_button` para el producto 4

### Requirement: Los IDs llegan al árbol de accesibilidad
Los IDs SHALL aparecer como `resource-id` en el árbol de `uiautomator` y de `maestro hierarchy`, incluidos los de las pestañas, los botones de diálogos y hojas, la casilla de registro y los desplegables.

#### Scenario: Pestañas
- **WHEN** hay una sesión iniciada
- **THEN** `tab_catalog`, `tab_cart` y `tab_account` están en el árbol

#### Scenario: Botón de un diálogo
- **WHEN** se muestra la alerta "Log out?"
- **THEN** los IDs de sus dos botones están en el árbol

#### Scenario: Casilla de registro
- **WHEN** se abre el registro
- **THEN** la casilla de términos aparece con su ID y su estado marcado o no marcado
