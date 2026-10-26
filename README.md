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

El puerto del servicio es el 8081 (`server.port` en `application.yml`). La vigencia de los enlaces de invitación, verificación y recuperación se configura en `rinde.iam.tokens`.

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

El servicio consume `SubscriptionActivated` y `SubscriptionSuspended` para reactivar o restringir la empresa. Hoy llegan como eventos de Spring. En la tarea S2-09 llegarán por RabbitMQ, y los consumidores ignorarán los eventos repetidos según su identificador.

## Estructura

El paquete `com.nexus.rinde.iam` se organiza en las capas `domain`, `application`, `infrastructure` e `interfaces`. El paquete `com.nexus.rinde` raíz permite que Spring cargue los beans de `rinde-shared-kernel`.
