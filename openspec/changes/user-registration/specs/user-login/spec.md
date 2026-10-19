# Spec Delta

## MODIFIED Requirements

### Requirement: Enlace a registro
La pantalla SHALL mostrar el enlace "Create account" (`login_register_link`), que MUST abrir la pantalla de registro. Al volver del registro al login, el email y la contraseña escritos antes MUST seguir escritos.

#### Scenario: Pulsar el enlace
- **WHEN** el usuario pulsa "Create account"
- **THEN** se muestra la primera pantalla del registro

#### Scenario: Volver del registro
- **WHEN** el usuario escribió un email en el login, abrió el registro y volvió
- **THEN** el email sigue escrito
