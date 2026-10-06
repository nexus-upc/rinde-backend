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

- [ ] POST /api/v1/trips (US14)
- [ ] PUT /api/v1/trips/{id}/assignment (US15)
- [ ] GET /api/v1/trips?status= (US16)
- [ ] GET /api/v1/trips/{id} (US17, US19)
- [ ] GET /api/v1/trips/assigned-to-me (US18)
- [ ] POST /api/v1/trips/{id}/start (US20)
- [ ] POST /api/v1/trips/{id}/finish (US20)
- [ ] Migración y pruebas `.feature`

## Farid Briceño

### Expense & Evidence (`expense`)

- [ ] POST /api/v1/trips/{tripId}/expenses (US21)
- [ ] POST /api/v1/expenses/evidences/presigned-url (US21)
- [ ] POST /api/v1/expenses/sync (US22)
- [ ] GET /api/v1/trips/{tripId}/expenses (US23)
- [ ] PATCH /api/v1/expenses/{id}/status (US24)
- [ ] GET /api/v1/expenses/{id} (US17)
- [ ] Migración y pruebas `.feature`

## Gabriel Espinar

### Settlement (`settlement`)

- [ ] POST /api/v1/settlements/advances (US35)
- [ ] GET /api/v1/settlements/{id} (US25)
- [ ] POST /api/v1/settlements/{id}/recalculation (US25)
- [ ] POST /api/v1/settlements/{id}/close (US26)
- [ ] GET /api/v1/settlements/{id}/export (US27)
- [ ] GET /api/v1/trips/{tripId}/settlement (US28)
- [ ] Migración y pruebas `.feature`

### Operations Dashboard (`dashboard`)

- [ ] GET /api/v1/dashboard/trips/{tripId}/summary (US17)
- [ ] GET /api/v1/dashboard/fleet-status (US37)
- [ ] GET /api/v1/dashboard/metrics (US37)
- [ ] Pruebas `.feature`

## Mathias Cárdenas

### Fleet & Maintenance (`fleet`)

- [ ] GET /api/v1/vehicles (US12)
- [ ] POST /api/v1/vehicles (US12)
- [ ] GET /api/v1/vehicles/{id} (US32)
- [ ] GET /api/v1/vehicles/{id}/health-status (US15, US34)
- [ ] GET /api/v1/drivers (US13)
- [ ] POST /api/v1/drivers (US13)
- [ ] GET /api/v1/drivers/{id}/eligibility (US13, US15)
- [ ] POST /api/v1/maintenances (US33)
- [ ] GET /api/v1/maintenances/alerts (US34)
- [ ] Migración y pruebas `.feature`

### Incident Management (`incident`)

- [ ] POST /api/v1/incidents (US29)
- [ ] GET /api/v1/incidents?tripId= (US30)
- [ ] GET /api/v1/incidents/{id} (US30)
- [ ] PATCH /api/v1/incidents/{id}/status (US31)
- [ ] Migración y pruebas `.feature`

## Flor Contreras

### Subscriptions & Billing (`subscription`)

- [ ] GET /api/v1/plans (US38)
- [ ] POST /api/v1/subscriptions (US38)
- [ ] GET /api/v1/subscriptions/{id} (US40)
- [ ] POST /api/v1/subscriptions/{id}/checkout (US39)
- [ ] POST /api/v1/billing/webhooks/payment (US39)
- [ ] Migración y pruebas `.feature`

### Notifications (`notification`)

- [ ] Escuchar TenantRegistered, UserInvited y PasswordResetRequested (correos de IAM)
- [ ] Escuchar TripAssigned, ExpenseObserved, MaintenanceDue, IncidentReported y SubscriptionExpiring (avisos)

## Todo el equipo

- [ ] Refactorizar a microservicios (un servicio por bounded context y API Gateway)
- [ ] Frontend web en Angular (inicio de sesión, registro de empresa y viajes)
