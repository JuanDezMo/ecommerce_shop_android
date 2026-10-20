#!/bin/bash
# Uso: ./run.sh <serial del emulador> [flujo.yaml ...]   (serial: el de `adb devices`, por ejemplo emulator-5554)
# Antes de cada flujo deja el backend en su estado semilla (POST /api/v1/test/reset) y concede a la app el
# permiso de red local (Android 17 muestra un diálogo del sistema que taparía la pantalla)
set -e
cd "$(dirname "$0")"
SERIAL="$1"; shift
FLOWS=("$@"); [ ${#FLOWS[@]} -eq 0 ] && FLOWS=(flows/*.yaml)
APP_ID="co.com.learningswift.shop"
mkdir -p reports

for flow in "${FLOWS[@]}"; do
  curl -s -X POST http://localhost:8080/api/v1/test/reset -o /dev/null -w "reset %{http_code}\n"
  adb -s "$SERIAL" shell pm grant "$APP_ID" android.permission.ACCESS_LOCAL_NETWORK
  maestro --device "$SERIAL" test "$flow" --format junit --output "reports/$(basename "$flow" .yaml).xml"
done
