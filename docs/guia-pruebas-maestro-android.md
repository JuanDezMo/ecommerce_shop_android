# Guía de pruebas con Maestro en Android

Actualizado: 2026-10-19

La carpeta [`maestro/`](../maestro/) es una copia de la de iOS (`../ecommerce_shop_ios/maestro/`) con el `appId` de Android. Los IDs y los textos son los del [catálogo](catalogo-ids-accesibilidad.md). La guía general (conceptos, estructura, trucos) está en la [guía de iOS](../../ecommerce_shop_ios/docs/guia-pruebas-maestro-ios.md); aquí solo está lo propio de Android.

## Estado (2026-10-19)

Pasan en el emulador Pixel 6a (Android 17) los flujos `00-humo`, `01-login`, `02-registro`, `03-catalogo-y-carrito-lleno` y `06-checkout-exito`, en tema claro. El `06` también pasa en tema oscuro y contra el APK de release minificado con R8. Sin probar: emulador API 34, flujos `04`, `05`, `07`, `08` y `09` (no existen ni en iOS) y los extras de arranque (no implementados).

## Cómo ejecutarlos

```bash
# Backend arriba (docker compose, perfil testing) y emulador encendido con la app debug instalada
./gradlew :app:installDebug
maestro/run.sh emulator-5554                          # todos los flujos
maestro/run.sh emulator-5554 flows/06-checkout-exito.yaml
```

`run.sh` hace, antes de cada flujo, `POST /api/v1/test/reset` y `adb shell pm grant … ACCESS_LOCAL_NETWORK` (en Android 17 el diálogo del sistema taparía la app y las peticiones a `10.0.2.2` se agotarían). Los informes quedan en `maestro/reports/` (no se commitean).

## Diferencias con los flujos de iOS

| Qué | iOS | Android |
| --- | --- | --- |
| `appId` | `co.com.learningswift.ecommerce-shop` | `co.com.learningswift.shop` |
| Teclado | Se baja arrastrando la lista o no hace falta | `- hideKeyboard` tras cada campo: el teclado tapa los de abajo y un toque ahí falla |
| Volver desde el detalle de la orden | `tapOn: "Order placed"` (botón con el título anterior) | `- back` (atrás del sistema) |
| Alertas | Por texto | Por texto, igual (los IDs de sus botones también llegan) |
| Selector de cantidad | `…_quantity_stepper-Increment` | Igual |

Un cambio de comportamiento que obligue a cambiar un flujo se hace en las dos carpetas el mismo día.

## Si falla un flujo

1. `adb exec-out screencap -p > fallo.png` justo después del fallo: casi siempre es el teclado o un diálogo.
2. `maestro hierarchy` o `adb shell uiautomator dump` para ver los `resource-id`.
3. Maestro guarda capturas y el log en `~/.maestro/tests/<fecha>/`.
