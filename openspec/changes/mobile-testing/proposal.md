# Propuesta: mobile-testing

## Why

La app tiene todas las pantallas (A1–A8), pero los IDs solo se comprueban en una dirección (app → catálogo), el APK de release con R8 nunca se ha ejecutado (`proguard-rules.pro` está vacío) y los flujos de Maestro no se han ejecutado en Android. Es la iteración A9 de `docs/plan-iteraciones-android.md`, **recortada por decisión del responsable**: los extras de arranque (`testSessionToken`, `testForceError`, etc.) quedan para otra iteración, y Maestro empieza con los flujos que ya pasan en iOS (`00`, `01`, `02`, `03`, `06`). La aprobación de esta propuesta sustituye a la revisión previa del plan para ese alcance.

## What Changes

- **IDs en las dos direcciones:** `TestTagsTest` comprueba también que todo ID del catálogo existe en la app; las excepciones se listan una a una con su motivo. Se verifica en el árbol de accesibilidad que llegan pestañas, diálogos, casilla y desplegables.
- **Release con R8:** reglas de `kotlinx.serialization`, Ktor y OkHttp en `proguard-rules.pro` y una ejecución real del APK minificado.
- **Maestro en el repositorio de Android:** carpeta `maestro/` propia (copia de la de iOS, con `appId` de Android), con `run.sh` que hace el `reset` del backend y concede el permiso de red local. Pasan `00`, `01`, `02`, `03` y `06` en el emulador, en tema claro y oscuro.
- **Cierre:** `lint` limpio, README y documentación de Maestro al día.

## Capabilities

### New Capabilities

- `ui-test-identifiers`: los IDs de la app y del catálogo coinciden y llegan al árbol de accesibilidad.
- `mobile-ui-flows`: los flujos de Maestro corren sobre Android con un `run.sh` que prepara el entorno.
- `release-build`: el APK de release minificado funciona.

### Modified Capabilities

Ninguna.

## Impact

- **Código modificado:** `app/proguard-rules.pro`, `TestTagsTest` y, si faltan IDs, `TestTags` y las pantallas. README.
- **Nuevo:** `maestro/` (config, `run.sh`, flujos, subflujos) y su guía en `docs/`.
- **Repositorio iOS:** no se modifica.
- **Dependencias:** ninguna nueva.
- **Fuera de alcance (se revisa después):** extras de arranque y sus escenarios (sesión expirada, error forzado, retraso, URL), flujos `04`, `05`, `07`, `08` y `09`, emulador API 34 adicional, persistir el token.
