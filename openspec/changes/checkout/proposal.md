# Propuesta: checkout

## Why

El carrito ya se edita y se ajusta al stock, pero "Checkout" abre una pantalla provisional: no se puede comprar. Es el flujo de mayor riesgo de la app (crea órdenes reales y descuenta stock en el backend, que no es idempotente) y el segundo formulario largo, que reutiliza el patrón del registro. Es la iteración A7 de `docs/plan-iteraciones-android.md`: fusiona las iteraciones iOS `checkout` y `order-confirmation`.

## What Changes

- **Formulario de checkout:** dirección (5–120), ciudad (2–60), código postal (3–10 letras, dígitos, espacios o guiones), país (menú con los seis de la app) y forma de pago (Card/Cash/Transfer, Card al inicio), con el total estimado y "Place order". Sin datos de tarjeta.
- **Validación y errores** como en el registro: botón siempre habilitado, resumen "N fields need attention", error bajo cada campo, desplazamiento al primero, revalidación al salir del campo.
- **Confirmar:** "Place order" valida y abre "Place order?" con "Estimated total: $X."; solo su botón "Place order" envía `POST orders`.
- **Respuestas:** 201 → se vacía el carrito y se muestra la confirmación con los datos del backend; `INSUFFICIENT_STOCK`, `PRODUCT_NOT_FOUND`, `EMPTY_ORDER`, `DUPLICATE_ORDER_ITEM` (y `VALIDATION_ERROR` sin campo) → "Can't place your order" con "Review cart"; `VALIDATION_ERROR` con campos → en su campo; red, 5xx u otros → alerta genérica. **El carrito solo se modifica con el 201** y **"Try again" no reenvía: reabre "Place order?"** (el backend no es idempotente).
- **Confirmación:** número de orden, líneas con subtotal y total de la respuesta; sin botón de atrás ni gesto de volver; "View order" (detalle provisional hasta A8) y "Done" (vuelve al carrito vacío y cambia a Catalog, que se recarga con el stock actualizado).
- **Modelos y datos:** `PaymentMethod`, `Order`/`OrderItem`/`Shipping`, `OrderRepository.createOrder`, `CheckoutField`/`CheckoutForm`/`CheckoutValidator`, `FieldErrorMapper` (ya genérico) con los campos del checkout.
- **IDs `Checkout.*` y `Confirmation.*`** en `TestTags`; textos de las alertas en `AlertText`.

## Capabilities

### New Capabilities

- `checkout`: formulario, reglas, confirmación previa y cómo se resuelve cada respuesta del backend sin perder el carrito.
- `order-confirmation`: la pantalla que cierra la compra y adónde lleva cada acción.

### Modified Capabilities

Ninguna.

## Impact

- **Código nuevo:** `data/model/{PaymentMethod,Order}.kt`, `data/remote/dto/{CreateOrderRequestDto,OrderDto}.kt`, `data/repository/OrderRepository.kt`, `features/checkout/{CheckoutField,CheckoutForm,CheckoutValidator,CheckoutUiState,CheckoutViewModel}.kt`, `features/confirmation/ConfirmationScreen.kt`, `features/orders/OrderDetailScreen.kt` (provisional).
- **Código modificado:** `features/checkout/CheckoutScreen.kt` (deja de ser provisional), `app/Routes.kt`, `app/ShopApp.kt`, `app/AppContainer.kt`, `app/ViewModelFactories.kt`, `core/errors/AlertText.kt`, `core/testing/TestTags.kt`.
- **Tests:** unitarios (`CheckoutValidatorTest`, `CheckoutFieldTest`, `OrderTest`, `CheckoutRequestDtoTest`) e integración (`CheckoutIntegrationTest`, backend real, **necesita `docker compose up`**; crea órdenes: `reset` antes de cada test).
- **Dependencias:** ninguna nueva.
- **Fuera de alcance:** detalle real de orden y "My orders" (A8), vaciar el carrito al cerrar sesión (A8), hooks de prueba para forzar errores en el checkout (A9), fechas del backend (A8).
