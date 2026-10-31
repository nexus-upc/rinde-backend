# rinde-expense-service

Servicio de Expense & Evidence de RINDE by NEXUS: gastos del viaje con su comprobante, sincronización offline y revisión. Usa `rinde-shared-kernel` 0.2.0.

## Ejecución

Requiere Java 21, PostgreSQL y RabbitMQ. Crear las bases `rinde_expense` y `rinde_expense_test`, copiar `.env.example` como `.env` y completar los valores (`DB_*`, `JWT_SECRET`, `IAM_SERVICE_URL`, `TRIP_SERVICE_URL` y `RABBITMQ_*`).

```
mvnw.cmd spring-boot:run
mvnw.cmd clean verify
```

Puerto 8084. Swagger: http://localhost:8084/swagger-ui.html. Flyway crea el esquema `expense`: gastos, evidencias y `processed_settlement_events`.

## Endpoints públicos

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/trips/{tripId}/expenses` | Registro del gasto por el conductor, idempotente por `idempotencyKey` (US21). |
| GET | `/api/v1/trips/{tripId}/expenses` | Gastos de un viaje (US23). |
| GET | `/api/v1/expenses/{id}` | Detalle del gasto. |
| PATCH | `/api/v1/expenses/{id}/status` | Aprobación u observación con motivo, por ADMINISTRATOR u OPERATIONS_MANAGER (US24). |
| POST | `/api/v1/expenses/sync` | Lote de gastos registrados sin conexión; informa los rechazados (US22). |
| POST | `/api/v1/expenses/evidences/presigned-url` | Enlace para subir la foto del comprobante (US21). |

Antes de registrar, el servicio consulta a Trip el estado del viaje y a IAM el estado de la empresa. Un gasto bloqueado por una liquidación cerrada ya no se modifica.

## Endpoints internos

Sin JWT y fuera del gateway. Base `/api/internal/v1/expenses/tenants/{tenantId}`.

| Ruta | Lo consume |
|---|---|
| `/trips/{tripId}/approved` | Settlement, para calcular la liquidación |
| `/trips/{tripId}` | Dashboard, para el resumen del viaje |
| (raíz) | Dashboard, para las métricas de la empresa |

## Eventos

Consume `settlement.closed` y bloquea los gastos del viaje. Registra el `eventId` en `processed_settlement_events`, así que una entrega repetida no tiene efecto. Los mensajes que fallan quedan en `rinde.events.expense.settlement-events.dead`.

## Límites conocidos

El enlace de subida de fotos se arma localmente; todavía no hay un proveedor de almacenamiento conectado.
