# RINDE Backend

Backend de RINDE by NEXUS: gestión de viajes, gastos, liquidaciones, flota e incidencias para pymes de transporte de carga.

Es el monolito modular original, dentro del monorepo. Hoy atiende Fleet, Expense, Settlement, Subscriptions, Notifications y Dashboard; Incident tiene solo su estructura. Lo común viene de la librería `rinde-shared-kernel` 0.2.0.

Los paquetes `trip` e `iam` que siguen aquí son copias antiguas: IAM y Trip ya funcionan en `services/iam` y `services/trip`, y el gateway no envía tráfico a estas copias. No usarlas ni modificarlas. Fleet y Expense se están trasladando a `services/fleet` y `services/expense`; su código se mantiene aquí hasta que esos servicios funcionen.

## Requisitos

- JDK 21
- PostgreSQL 16 o superior
- RabbitMQ activo en el puerto 5672

## Base de datos

Crea las dos bases en tu PostgreSQL:

```sql
CREATE DATABASE rinde;
CREATE DATABASE rinde_test;
```

## Configuración

Los datos de conexión y los secretos se leen del archivo `.env` en la raíz del proyecto, que no se sube al repositorio. Copia `.env.example` como `.env` y completa los valores.

## Ejecutar

El webhook de pago simulado usa la variable `RINDE_BILLING_SIMULATION_WEBHOOK_SECRET`, definida en el `.env`. Configura el mismo valor en el entorno de Postman que firme los webhooks.

```
mvnw.cmd spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html

Pruebas:

```
mvnw.cmd clean verify
```

## Notifications y RabbitMQ (S2-08)

Notifications recibe JSON desde el exchange durable `rinde.events`, con la routing key
`trip.assigned` y la cola durable `notification.trip-assigned`. El contrato conserva `eventId`,
`occurredAt`, `tenantId`, `tripId`, `code`, `destination`, `departureDate`, `vehicleId` y `driverId`.
El consumidor usa un DTO propio y no necesita cargar clases Java del servicio Trip.

`eventId` identifica el aviso en `notification.retry_store`. Una entrega repetida conserva el
registro y no vuelve a enviar el aviso. El adaptador push sigue sin proveedor: deja el aviso
pendiente y aplica las esperas de 1, 2, 4, 8 y 16 minutos. No se presenta como un envío push real.

Un fallo al guardar el aviso reintenta el mensaje hasta tres veces; al agotarse queda en
`notification.trip-assigned.dead`. Un JSON inválido también se conserva en esa cola.
El listener local se mantiene durante la migración para los viajes que aún se asignen en el monolito.
Si el mismo evento llega por ambos canales, usa el mismo registro de idempotencia.

La conexión se configura con las variables `RABBITMQ_*` de `.env.example`. Las pruebas de mensajería
usan colas aisladas con nombres aleatorios y requieren RabbitMQ y `rinde_test` disponibles.

## Suscripciones e IAM separado (S2-09)

Después del commit del pago o del cambio de estado, se publican `subscription.activated` y
`subscription.suspended` en `rinde.events`. El JSON contiene `eventId`, `occurredAt` y `tenantId`.
El productor declara también la cola durable `iam.subscription-events` y sus bindings, de modo
que el mensaje quede guardado aunque IAM aún no haya arrancado. Ambos procesos deben usar los
mismos valores de `RINDE_EVENTS_EXCHANGE` y `RINDE_IAM_SUBSCRIPTION_QUEUE`.

Se solicitan confirmaciones del broker y devolución de mensajes sin ruta. Los fallos de conexión
se reintentan hasta tres veces. Si no se confirma la publicación, el error registra el eventId
y requiere conciliación: no hay outbox transaccional ni recuperación automática de una caída
del proceso entre el commit y la publicación. Esa garantía queda fuera del Sprint 2.

IAM aplica el mensaje y registra su identificador en su propia base. Notifications y Fleet
mantienen los eventos locales que ya consumían; no se eliminan contextos del monolito.
Las pruebas de esta tarea verifican el pago, el contrato del productor, los commits y rollbacks,
y el consumidor de IAM por separado.

## Integración de la web con IAM y suscripciones

Por defecto, `RINDE_IAM_REMOTE_ENABLED=true` selecciona la fachada HTTP de IAM y
`IAM_SERVICE_URL` apunta a `http://localhost:8081`. Los contextos del monolito consultan usuarios,
estado de empresa y correo del administrador por HTTP. No se copian filas de IAM a la base local.
Si IAM no responde, la API informa 503. El listener local de suscripciones de IAM queda desactivado
en este modo: la actualización de la empresa llega al servicio separado por RabbitMQ.

Para conservar la ejecución histórica del monolito completo puede usarse
`RINDE_IAM_REMOTE_ENABLED=false`. Las pruebas existentes seleccionan este modo expresamente.

`GET /api/v1/subscriptions/current` consulta la suscripción de la empresa autenticada y devuelve
204 cuando aún no tiene una. Prioriza la suscripción pendiente de pago frente a una anterior;
de otro modo elige la de vigencia más reciente. Cada consulta sigue limitada a su empresa.

El checkout agrega `confirmationEnabled`. Solo con `RINDE_BILLING_SIMULATION_ENABLED=true`
se registra `POST /api/v1/subscriptions/{id}/payments/{paymentId}/simulation`, que recibe
`{"status":"CONFIRMED"}` o `{"status":"REJECTED"}`. Exige administrador y valida la empresa y la
suscripción del pago. Reutiliza el procesamiento idempotente del webhook sin entregar su clave
HMAC a la web. La simulación está desactivada por defecto y debe habilitarse solo para pruebas.
