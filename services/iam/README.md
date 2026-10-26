# rinde-iam-service

Servicio de IAM & Tenancy de RINDE by NEXUS: empresas, usuarios, roles, inicio de sesión y emisión de los tokens JWT. Es el primer servicio extraído del monolito modular `rinde-backend` (Sprint 2, tarea S2-02). Usa la librería `rinde-shared-kernel` (com.nexus:rinde-shared-kernel:0.1.0).

## Requisitos

- Java 21.
- PostgreSQL 17 en local (sin Docker).
- La librería `rinde-shared-kernel` 0.1.0 instalada en el repositorio local de Maven, o disponible en GitHub Packages.

## Configuración

Los secretos y los datos de conexión se leen del archivo `.env` en la raíz del proyecto, que Git ignora. Para crearlo, copia `.env.example` y completa sus valores.

| Variable | Descripción |
| --- | --- |
| `DB_URL` | URL JDBC de la base del servicio, por ejemplo `jdbc:postgresql://localhost:5432/rinde_iam`. |
| `DB_USERNAME` | Usuario de PostgreSQL. |
| `DB_PASSWORD` | Contraseña de PostgreSQL. |
| `JWT_SECRET` | Secreto de firma de los tokens. Debe ser el mismo que usa el monolito y tener al menos 32 caracteres. |
| `WEB_BASE_URL` | URL de la web Angular, por ejemplo `http://localhost:4200`. Es la base de los enlaces de acceso del log. Si no se define, toma ese valor (`rinde.web.base-url`). |

El puerto del servicio es el 8081 (`server.port` en `application.yml`). La vigencia de los enlaces de invitación, verificación y recuperación se configura en `rinde.iam.tokens`.

### Enlaces de acceso para la web

Hasta que Notifications envíe los correos, el servicio registra los enlaces de acceso en el log. En INFO solo aparece el token enmascarado. En DEBUG aparece el enlace completo que usará la web:

| Enlace | Formato |
| --- | --- |
| Verificación de la empresa | `{WEB_BASE_URL}/verify-email?tenant={tenantId}&token={token}` |
| Invitación de usuario | `{WEB_BASE_URL}/accept-invitation?token={token}` |
| Recuperación de contraseña | `{WEB_BASE_URL}/reset-password?token={token}` |

Para ver los enlaces, activa el nivel DEBUG de la clase que los registra: `logging.level.com.nexus.rinde.iam.application.internal.eventhandlers.AccessTokenLoggingEventHandler=DEBUG`.

## Base de datos

El servicio usa su propia base y su propio esquema `iam`. Crea las bases `rinde_iam` (servicio) y `rinde_iam_test` (pruebas) en PostgreSQL:

```sql
CREATE DATABASE rinde_iam;
CREATE DATABASE rinde_iam_test;
```

Al arrancar, Flyway crea el esquema `iam` y aplica las migraciones de `src/main/resources/db/migration/iam`.

## Ejecución

```
mvnw.cmd spring-boot:run
```

Con el servicio arriba, la documentación está en `http://localhost:8081/swagger-ui.html` y el estado en `http://localhost:8081/actuator/health`.

## Pruebas

```
mvnw.cmd clean verify
```

Ejecuta las pruebas unitarias, las de integración (contra `rinde_iam_test`) y las de aceptación de `src/test/resources/features/iam`.

## Endpoints

Endpoints públicos. Los de acceso y registro no llevan JWT; el resto requiere `Authorization: Bearer <token>`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/v1/auth/sign-in` | Inicio de sesión (US09). |
| POST | `/api/v1/auth/password-reset` | Solicitud de recuperación de contraseña (US10). |
| POST | `/api/v1/auth/password` | Definición de contraseña con el enlace recibido (US10 y US11). |
| POST | `/api/v1/tenants` | Registro de empresa y de su administrador (US08). |
| POST | `/api/v1/tenants/{id}/verification` | Verificación del correo de la empresa (US08). |
| GET | `/api/v1/users/me` | Perfil del usuario que inicia sesión, con los datos de su empresa (cualquier rol). Responde `{id, fullName, email, role, status, tenant: {id, tradeName, ruc, status}}`. |
| GET | `/api/v1/users` | Listado de usuarios de la empresa (US11). |
| POST | `/api/v1/users` | Invitación de usuario (US11). |
| PATCH | `/api/v1/users/{id}` | Cambio de rol o de estado de un usuario (US11). |

Endpoints internos. Los llaman otros servicios de RINDE sin JWT, por eso el gateway no debe enrutar `/api/internal`. Están documentados en Swagger con la etiqueta `Internal`.

| Método | Ruta | Respuesta |
| --- | --- | --- |
| GET | `/api/internal/v1/tenants/{tenantId}/status` | `{tenantId, status}`, con el estado actual de la empresa. |
| GET | `/api/internal/v1/tenants/{tenantId}/users/{userId}` | Datos del usuario: `userId`, `tenantId`, `fullName`, `email`, `role` y `status`. |
| GET | `/api/internal/v1/tenants/{tenantId}/users/{userId}/active-role?role=DRIVER` | `{active}`, que indica si el usuario está activo y tiene el rol. |
| GET | `/api/internal/v1/tenants/{tenantId}/administrator-email` | `{email}`, con el correo del administrador activo. |

Las respuestas de error siguen el formato `application/problem+json`. Un recurso de otra empresa responde 404.

## Eventos

El servicio recibe JSON desde el exchange durable `rinde.events`, con las routing keys
`subscription.activated` y `subscription.suspended`. Ambas llegan a la cola durable
`iam.subscription-events`, con un consumidor y prefetch de uno. El contrato propio contiene
`eventId`, `occurredAt` y `tenantId`; no se cargan clases Java del productor.

La migración V2 crea `iam.subscription_event_receipts`. La recepción bloquea la empresa,
aplica el estado y guarda el identificador del evento en una sola transacción. Los duplicados
se ignoran incluso después de otro cambio de estado; tampoco se aplica un evento con fecha
anterior al último recibido para esa empresa. Los eventos no activan empresas pendientes de
verificar el correo. Se conservan los listeners locales para las pruebas y la transición.

Los fallos se reintentan hasta tres veces. Si falta la empresa o el JSON no es válido, el
mensaje queda en `iam.subscription-events.dead`, para inspección y reenvío con el mismo eventId
después de corregir la causa. Las variables `RABBITMQ_*` y `RINDE_IAM_SUBSCRIPTION_QUEUE` están en
`.env.example`. Las pruebas de mensajería requieren RabbitMQ y usan colas aleatorias aisladas.

## Estructura

El paquete `com.nexus.rinde.iam` se organiza en las capas `domain`, `application`, `infrastructure` e `interfaces`. El paquete `com.nexus.rinde` raíz permite que Spring cargue los beans de `rinde-shared-kernel`.
