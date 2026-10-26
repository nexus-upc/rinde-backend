# RINDE Backend

Backend de RINDE by NEXUS: gestión de viajes, gastos, liquidaciones, flota e incidencias para pymes de transporte de carga.

Es un monolito modular en Spring Boot. Cada bounded context tiene su paquete (`iam`, `trip`, `expense`, `settlement`, `fleet`, `incident`, `subscription`, `notification`, `dashboard`) y lo común está en `shared`.

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

## Tareas

El avance de cada integrante está en [TODO.md](TODO.md).

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
