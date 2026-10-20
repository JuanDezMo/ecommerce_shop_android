# Spec Delta

## ADDED Requirements

### Requirement: Fechas con hora del backend
La app SHALL leer `createdAt` en UTC con 0 a 9 decimales de segundo, conservando al menos los milisegundos. Un texto que no sea una fecha válida MUST hacer fallar la lectura de esa respuesta, que termina como error desconocido.

#### Scenario: Sin decimales
- **WHEN** llega `2026-10-14T15:04:05Z`
- **THEN** se lee como el 14 de octubre de 2026, 15:04:05 UTC

#### Scenario: Con seis o nueve decimales
- **WHEN** llega `2026-10-14T15:04:05.123456Z` o `2026-10-14T15:04:05.123456789Z`
- **THEN** se lee como 15:04:05.123 UTC o con más precisión, sin perder el instante

#### Scenario: Texto inválido
- **WHEN** llega `not-a-date`
- **THEN** la lectura de la respuesta falla

### Requirement: Formato de las fechas que ve el usuario
Las fechas SHALL mostrarse en inglés de Estados Unidos y en la zona horaria del dispositivo: `Sep 15, 2026` en las listas y `Sep 15, 2026, 11:30 AM` (hora de 12 h con AM/PM y un espacio normal antes) en el detalle. Las unidades SHALL mostrarse como "1 item" o "N items".

#### Scenario: Zona del dispositivo
- **WHEN** llega `2026-09-01T03:00:00Z` y el dispositivo está en Bogotá (UTC-5)
- **THEN** la lista muestra `Aug 31, 2026` y el detalle `Aug 31, 2026, 10:00 PM`

#### Scenario: Cambio de día en otra zona
- **WHEN** llega la misma fecha y el dispositivo está en Auckland (UTC+12)
- **THEN** la lista muestra `Sep 1, 2026` y el detalle `Sep 1, 2026, 3:00 PM`

#### Scenario: Unidades
- **WHEN** una orden tiene 1 unidad o 3 unidades
- **THEN** se muestra "1 item" o "3 items"
