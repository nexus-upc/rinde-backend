# RINDE Services

Backend de RINDE by NEXUS. Cada carpeta es un componente con su propio pom.xml, wrapper de Maven, `.env`, base de datos, Dockerfile y versión. Los clientes están en [nexus-upc/rinde-apps](https://github.com/nexus-upc/rinde-apps).

**Trabajar siempre desde `develop`.** La rama `main` conserva el monolito anterior (v0.2.0) y no se usa para desarrollar.

## Componentes

| Carpeta | Artefacto | Versión | Puerto | Base | Estado |
|---|---|---|---|---|---|
| libraries/shared-kernel | com.nexus:rinde-shared-kernel | 0.2.0 | No aplica | No aplica | Publicada en GitHub Packages. |
| services/iam | rinde-iam-service | 0.3.0 | 8081 | rinde_iam | Separado. |
| services/trip | rinde-trip-service | 0.2.0 | 8082 | rinde_trip | Separado. |
| services/fleet | rinde-fleet-service | 0.1.0 | 8083 | rinde_fleet | Esqueleto, pendiente de S2-04. |
| services/expense | rinde-expense-service | 0.1.0 | 8084 | rinde_expense | Esqueleto, pendiente de S2-06. |
| services/core | rinde-backend | 0.3.0 | 8080 | rinde | Contextos que siguen en el monolito. |
| gateway | rinde-gateway | 0.3.0 | 8000 | No aplica | Punto de entrada y Swagger. |

Cada README de carpeta tiene la configuración, los endpoints, los contratos internos y los eventos de ese componente.

Core todavía conserva copias antiguas de los paquetes `trip` e `iam`. El gateway ya no les envía tráfico, así que no reciben datos nuevos: no usarlas ni modificarlas. Lo que un contexto necesite de Trip o IAM se pide por HTTP a su servicio o llega por RabbitMQ.

## Empezar

```powershell
git clone https://github.com/nexus-upc/rinde-services.git
cd rinde-services
git switch develop
git pull --ff-only origin develop
git switch -c feature/<tarea>
```

## Compilar y probar

Requiere Java 21, PostgreSQL y RabbitMQ. Cada servicio lee su propio `.env`: copiar su `.env.example` dentro de su carpeta y completar los valores. Crear las bases de prueba de cada servicio (`rinde_test`, `rinde_iam_test`, `rinde_trip_test` y las de los servicios nuevos).

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd -pl services/trip -am clean verify
```

El primer comando prueba todo el reactor; el segundo, un solo servicio con la librería.

## Docker Compose

```powershell
Copy-Item infra/.env.example .env.compose
docker compose --env-file .env.compose build
docker compose --env-file .env.compose up -d
```

Crea el gateway, IAM, Trip, core, RabbitMQ y una instancia de PostgreSQL por servicio. Las bases y el broker no se publican al host y los puertos HTTP escuchan solo en 127.0.0.1. Swagger de todos los servicios: http://localhost:8000/swagger-ui.html.

`pwsh -File tests/deployment/verify.ps1 -Build` levanta un entorno temporal, comprueba el flujo entre procesos y lo elimina. Requiere PowerShell 7.2 o superior.

## Reglas

- Ramas `feature/<tarea>` desde `develop` e integración con `merge --no-ff`. Conventional Commits en inglés, solo título.
- Un servicio no importa clases de otro contexto: usa HTTP con DTO propios o eventos de RabbitMQ, e ignora eventos repetidos por `eventId`.
- Ningún servicio lee las tablas de otro.
- `mvnw.cmd clean verify` en verde antes de pedir la integración.
- Tags por componente para nuevas versiones (`iam/vX.Y.Z`, `trip/vX.Y.Z`, `shared-kernel/vX.Y.Z`). Los tags históricos `v0.1.0` y `v0.2.0` no se cambian.

## Límites conocidos

- Pagos simulados: no hay pasarela bancaria. Correo y push sin proveedor; IAM puede guardar los enlaces de acceso en un buzón de demostración (ver `services/iam`).
- Los eventos se publican después del commit, sin outbox: una caída entre ambos pasos puede perder un evento.
- Fleet, Expense, Settlement y Dashboard todavía funcionan dentro de core.
