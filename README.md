# RINDE Services

Monorepo del backend de RINDE by NEXUS. La web, la aplicación móvil y la landing permanecen fuera de este repositorio. La rama develop contiene la migración; main conserva la entrega v0.2.0 hasta el cierre del sprint.

## Componentes

| Carpeta | Artefacto Maven | Versión | Puerto | Base |
|---|---|---|---|---|
| libraries/shared-kernel | com.nexus:rinde-shared-kernel | 0.1.0 | No aplica | No aplica |
| services/iam | com.nexus:rinde-iam-service | 0.1.0 | 8081 | rinde_iam |
| services/core | com.nexus:rinde-backend | 0.2.0 | 8080 | rinde |
| services/trip | com.nexus:rinde-trip-service | 0.1.0 | 8082 | rinde_trip |
| gateway | com.nexus:rinde-gateway | 0.1.0 | 8000 | No aplica |

Core conserva los contextos del monolito durante la transición. Fleet y Expense aún deben separarse. Trip ya tiene cliente HTTP y mensajería, pero la asignación necesita los endpoints internos de Fleet. El monorepo no resuelve esa dependencia ni el cierre distribuido pendiente de Settlement.

Cada servicio tiene su propio pom.xml, wrapper, configuración, migraciones, pruebas y Dockerfile. Los pom.xml de los componentes no heredan del agregador raíz y sus versiones son independientes. Shared-kernel se consume como dependencia Maven versionada, sin copiar sus fuentes dentro de los servicios. El reactor permite compilar el conjunto sin descargar esa librería de GitHub Packages.

## Trabajo del equipo

Clonar este repositorio y trabajar desde develop:

```powershell
git clone https://github.com/nexus-upc/rinde-services.git
cd rinde-services
git switch develop
git pull --ff-only origin develop
git switch -c feature/fleet-availability
```

Las nuevas implementaciones van aquí: Fleet en services/fleet y Expense en services/expense. Añadir su módulo al agregador al crearlo. Los repositorios separados anteriores se conservan como referencia; no continuar cambios en ambos lugares.

Usar ramas feature/ por tarea, pruebas antes de integrar y merge --no-ff a develop. Main se actualizará mediante una release posterior. No usar squash al integrar historiales. Cada integrante conserva su identidad de Git. Usar tags por componente para las siguientes versiones: iam/vX.Y.Z, trip/vX.Y.Z, core/vX.Y.Z, gateway/vX.Y.Z y shared-kernel/vX.Y.Z. No cambiar los tags históricos v0.1.0 y v0.2.0.

## Compilación y pruebas locales

Requiere Java 21, PostgreSQL y RabbitMQ. Crear las bases de prueba rinde_test, rinde_iam_test y rinde_trip_test. Cada servicio carga su propio archivo .env: copiar su .env.example dentro de su carpeta y completar los valores. No usar un .env común para las conexiones de distintos servicios.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd -pl libraries/shared-kernel install
```

El primer comando prueba todos los módulos. El segundo instala la librería para ejecutar o compilar un servicio por separado. Las pruebas de Trip simulan IAM y Fleet con WireMock; los tests de persistencia y mensajería usan PostgreSQL y RabbitMQ reales.

Para ejecutar un servicio, entrar en su carpeta:

```powershell
cd services/iam
.\mvnw.cmd spring-boot:run
```

Los README de cada componente conservan sus endpoints y contratos. Para consultar el historial anterior a la migración, usar los commits y repositorios indicados en migration-sources.json. Los archivos originales estaban en la raíz de cada repositorio; git log --all muestra también esos commits originales.

## Contenedores

Desde la raíz, con Docker Desktop activo:

```powershell
Copy-Item infra/.env.example .env.compose
# Completar .env.compose con valores locales.
docker compose --env-file .env.compose build
docker compose --env-file .env.compose up -d
docker compose --env-file .env.compose ps
docker compose --env-file .env.compose down
```

La configuración crea una instancia de PostgreSQL y un volumen distintos por servicio, más RabbitMQ. No publica las bases ni el broker al host. Los puertos HTTP se enlazan solo a 127.0.0.1. Cada imagen puede construirse y desplegarse por separado:

```powershell
docker build -f services/iam/Dockerfile -t rinde/iam:0.1.0 .
```

El contexto de build es la raíz del monorepo. El Dockerfile de cada servicio compila shared-kernel como artefacto Maven antes de compilar el servicio. El runtime solo contiene su JAR y corre con usuario sin privilegios. El gateway no requiere shared-kernel.

El acceso a Swagger se mantiene en http://localhost:8000/swagger-ui.html. El checkout usa pago simulado solo si se habilita RINDE_BILLING_SIMULATION_ENABLED; no hay proveedor de pago ni push real. Las imágenes se construyen sin ejecutar pruebas de integración, que deben pasar con el comando local antes de integrar.

## Historial y versiones

La migración incorpora las ramas publicadas de shared-kernel, IAM, gateway y Trip mediante merges que conservan los commits originales. Los autores, fechas y hashes de esos commits no se modifican. Los tags de origen se incorporan con un prefijo por componente para evitar colisiones; las releases publicadas antes de la migración siguen en sus repositorios de origen.

La publicación de shared-kernel en GitHub Packages sigue pendiente. El reactor y la instalación local permiten desarrollar mientras se habilita ese acceso. No desplegar un servicio nuevo reutilizando las tablas de otro servicio.
