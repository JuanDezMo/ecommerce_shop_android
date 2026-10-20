# release-build Specification

## Purpose
TBD - created by archiving change mobile-testing. Update Purpose after archive.

## Requirements

### Requirement: El APK de release minificado funciona
La variante release SHALL compilar con R8 activo y sus reglas MUST conservar lo que `kotlinx.serialization`, Ktor y OkHttp necesitan en tiempo de ejecución. El APK minificado SHALL ejecutarse en un emulador y completar login, catálogo, carrito, compra y "My orders" sin errores de clases o de serialización.

#### Scenario: Recorrido con el APK minificado
- **WHEN** se instala el APK de release y se inicia sesión con `standard@shoplab.dev`
- **THEN** el catálogo carga, se puede añadir al carrito y comprar, y la orden aparece en "My orders"

#### Scenario: Registro de errores limpio
- **WHEN** termina el recorrido
- **THEN** `adb logcat` no contiene `ClassNotFoundException` ni `SerializationException` de la app

#### Scenario: Cada regla tiene motivo
- **WHEN** se revisa `proguard-rules.pro`
- **THEN** cada regla tiene un comentario que explica qué protege
