# TODO

Marca con `[x]` lo que termines, en el mismo commit donde lo terminas.

Meta del Sprint 1: al menos 12 endpoints funcionando, con su prueba `.feature`.

## Pedro Lecca

### IAM & Tenancy (`iam`)

- [x] POST /api/v1/tenants (US08)
- [x] POST /api/v1/tenants/{id}/verification (US08)
- [x] POST /api/v1/auth/sign-in (US09)
- [x] POST /api/v1/auth/password-reset (US10)
- [x] POST /api/v1/auth/password (US10, US11)
- [x] GET /api/v1/users (US11)
- [x] POST /api/v1/users (US11)
- [x] PATCH /api/v1/users/{id} (US11)
- [x] Migración y pruebas `.feature`

### Trip Management (`trip`)

- [x] POST /api/v1/trips (US14)
- [x] PUT /api/v1/trips/{id}/assignment (US15)
- [x] GET /api/v1/trips?status= (US16)
- [x] GET /api/v1/trips/{id} (US17, US19)
- [x] GET /api/v1/trips/assigned-to-me (US18)
- [x] POST /api/v1/trips/{id}/start (US20)
- [x] POST /api/v1/trips/{id}/finish (US20)
- [x] Migración y pruebas `.feature`

## Farid Briceño

### Expense & Evidence (`expense`)

- [x] POST /api/v1/trips/{tripId}/expenses (US21)
- [x] POST /api/v1/expenses/evidences/presigned-url (US21)
- [x] POST /api/v1/expenses/sync (US22)
- [x] GET /api/v1/trips/{tripId}/expenses (US23)
- [x] PATCH /api/v1/expenses/{id}/status (US24)
- [x] GET /api/v1/expenses/{id} (US17)
- [x] Migración y pruebas `.feature`

## Gabriel Espinar

### Settlement (`settlement`)

- [x] POST /api/v1/settlements/advances (US35)
- [x] GET /api/v1/settlements/{id} (US25)
- [x] POST /api/v1/settlements/{id}/recalculation (US25)
- [x] POST /api/v1/settlements/{id}/close (US26)
- [x] GET /api/v1/settlements/{id}/export (US27)
- [x] GET /api/v1/trips/{tripId}/settlement (US28)
- [x] Migración y pruebas `.feature`

### Operations Dashboard (`dashboard`)

- [x] GET /api/v1/dashboard/trips/{tripId}/summary (US17)
- [x] GET /api/v1/dashboard/fleet-status (US37)
- [x] GET /api/v1/dashboard/metrics (US37)
- [x] Pruebas `.feature`

## Mathias Cárdenas

### Fleet & Maintenance (`fleet`)

- [x] GET /api/v1/vehicles (US12)
- [x] POST /api/v1/vehicles (US12)
- [x] GET /api/v1/vehicles/{id} (US32)
- [x] GET /api/v1/vehicles/{id}/health-status (US15, US34)
- [x] GET /api/v1/drivers (US13)
- [x] POST /api/v1/drivers (US13)
- [x] GET /api/v1/drivers/{id}/eligibility (US13, US15)
- [x] POST /api/v1/maintenances (US33)
- [x] GET /api/v1/maintenances/alerts (US34)
- [x] GET /api/v1/drivers/{id} (US13)
- [x] GET /api/v1/vehicles/{id}/maintenance-status (US34)
- [x] POST /api/v1/vehicles/{id}/maintenances (US33)
- [x] Escuchar `TripStarted` y `TripFinished` para pasar la unidad a IN_TRIP y devolverla a AVAILABLE
- [x] Publicar `MaintenanceDue` cuando un mantenimiento pasa a próximo o vencido
- [ ] Escenarios `.feature` de US32 y US33
- [x] Migración y pruebas `.feature`

### Incident Management (`incident`)

Pendiente para el Sprint 2. Ningún otro contexto depende de él todavía.

- [ ] POST /api/v1/incidents (US29)
- [ ] GET /api/v1/incidents?tripId= (US30)
- [ ] GET /api/v1/incidents/{id} (US30)
- [ ] PATCH /api/v1/incidents/{id}/status (US31)
- [ ] Migración y pruebas `.feature`

## Flor Contreras

### Subscriptions & Billing (`subscription`)

- [x] GET /api/v1/plans (US38)
- [x] POST /api/v1/subscriptions (US38)
- [x] GET /api/v1/subscriptions/{id} (US40)
- [x] POST /api/v1/subscriptions/{id}/checkout (US39; checkout simulado sin datos de tarjeta)
- [x] POST /api/v1/billing/webhooks/payment (US39; firma HMAC, rechazo, reintento, confirmación e idempotencia)
- [x] Migraciones y escenarios `.feature` para US38, US39, US40 y US36
- [x] Completar US40: aviso de renovación a cinco días, gracia de tres días, restricciones de altas y límite de Fleet con sugerencia de plan
- [x] Ejecutar la suite de aceptación contra PostgreSQL 17.11 (120 escenarios y 1291 pasos aprobados; 0 fallos)
- [x] Ejecutar la colección exportada con Postman CLI 1.70.0 en `rinde_test` (17 solicitudes y 30 aserciones aprobadas; comprobante simulado verificado y datos temporales limpiados)
- [x] Ejecutar la colección local activa con Postman CLI 1.70.0 en `rinde_test` (19 solicitudes y 33 comprobaciones aprobadas; la suscripción, el pago confirmado y el comprobante quedaron persistidos)
- [x] Ejecutar la colección local desde Postman Runner (19 solicitudes y 33 comprobaciones aprobadas; capturas guardadas fuera del repositorio)

### Notifications (`notification`)

- [ ] Escuchar TenantRegistered, UserInvited y PasswordResetRequested (correos de IAM)
- [x] Escuchar `TripAssigned` y guardar un aviso idempotente pendiente en `notification.retry_store`
- [x] Reintentar los avisos y correos fallidos: 5 reintentos con esperas de 1, 2, 4, 8 y 16 minutos
- [ ] Configurar el token del dispositivo y un proveedor push real para `TripAssigned`
- [x] Escuchar `SubscriptionExpiring` y registrar el correo de renovación en la bandeja de salida simulada, de forma idempotente
- [x] Escuchar `MaintenanceDue` y avisar por correo al administrador
- [ ] Escuchar ExpenseObserved e IncidentReported (avisos)

## Todo el equipo

- [x] Comunicación entre contextos solo por `interfaces/acl` y eventos (las fachadas devuelven resúmenes, no agregados)
- [x] Secretos en `.env` y `application.yml` en el repositorio
- [ ] Refactorizar a microservicios (un servicio por bounded context y API Gateway)
- [ ] Frontend web en Angular (inicio de sesión, registro de empresa y viajes)
