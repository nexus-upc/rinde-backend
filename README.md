# RINDE Services

Monorepo del backend de RINDE by NEXUS. La web, la aplicación móvil y la landing permanecen fuera de este repositorio. La rama develop contiene la migración; main conserva la entrega v0.2.0 hasta el cierre del sprint.

## Componentes

| Carpeta | Artefacto Maven | Versión | Puerto | Base |
|---|---|---|---|---|
| libraries/shared-kernel | com.nexus:rinde-shared-kernel | 0.2.0 | No aplica | No aplica |
| services/iam | com.nexus:rinde-iam-service | 0.2.0 | 8081 | rinde_iam |
| services/core | com.nexus:rinde-backend | 0.3.0 | 8080 | rinde |
| services/trip | com.nexus:rinde-trip-service | 0.2.0 | 8082 | rinde_trip |
| gateway | com.nexus:rinde-gateway | 0.2.0 | 8000 | No aplica |

Core conserva los contextos del monolito durante la transición. Fleet y Expense aún deben separarse. Trip usa el contrato HTTP real de Fleet, expuesto temporalmente desde core. Se comprobó asignación, inicio, fin y actualización de la unidad por RabbitMQ. La extracción de Fleet sigue pendiente. El monorepo no resuelve esa dependencia ni el cierre distribuido pendiente de Settlement.

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
docker build -f services/iam/Dockerfile -t rinde/iam:0.2.0 .
```

El contexto de build es la raíz del monorepo. El Dockerfile de cada servicio compila shared-kernel como artefacto Maven antes de compilar el servicio. El runtime solo contiene su JAR y corre con usuario sin privilegios. El gateway no requiere shared-kernel.

El acceso a Swagger se mantiene en http://localhost:8000/swagger-ui.html. El checkout usa pago simulado solo si se habilita RINDE_BILLING_SIMULATION_ENABLED; no hay proveedor de pago ni push real. Las imágenes se construyen sin ejecutar pruebas de integración, que deben pasar con el comando local antes de integrar.

La prueba de despliegue está dentro del repositorio y requiere PowerShell 7.2 o superior:

```powershell
pwsh -File tests/deployment/verify.ps1 -Build
```

Crea un proyecto Compose temporal con credenciales generadas, puertos libres y bases nuevas. Comprueba el arranque, OpenAPI, los límites del gateway, registro, verificación, ingreso, perfil, consulta de suscripción y programación de un viaje. Prepara el hash del token solo en la base efímera para no depender del envío de correo. También comprueba el usuario del runtime y que IAM no copie su empresa a core. Al terminar retira sus contenedores, volúmenes y archivo de secretos temporal; guarda el resultado en tests/deployment/target/result.json, ignorado por Git. También comprueba asignación, inicio y fin contra Fleet en core, sus eventos y el aviso pendiente. El cierre distribuido de Settlement sigue pendiente.

## Historial y versiones

La migración incorpora las ramas publicadas de shared-kernel, IAM, gateway y Trip mediante merges que conservan los commits originales. Los autores, fechas y hashes de esos commits no se modifican. Los tags de origen se incorporan con un prefijo por componente para evitar colisiones; las releases publicadas antes de la migración siguen en sus repositorios de origen.

La publicación de shared-kernel en GitHub Packages se ejecuta con el workflow publish-shared-kernel.yml, con permiso packages:write limitado al job. El reactor y la instalación local permiten desarrollar mientras se habilita ese acceso. No desplegar un servicio nuevo reutilizando las tablas de otro servicio.

## Administración de Rinde

El dueño usa /platform/sign-in en la web y POST /api/v1/platform/auth/sign-in en IAM. Es una identidad de plataforma sin tenantId, almacenada fuera de iam.users. El rol PLATFORM_OWNER no pertenece al catálogo de roles de empresa y no puede asignarse con invitaciones. El token tiene emisor y audiencia de plataforma, firma independiente y vigencia de 15 minutos. Gateway y servicios rechazan el intercambio entre ambos ámbitos.

Configurar RINDE_PLATFORM_JWT_SECRET con un valor distinto de JWT_SECRET en IAM, core y gateway. Sin esa configuración no se habilita el ingreso del dueño. Para el alta inicial, configurar solo en el entorno privado de IAM RINDE_PLATFORM_BOOTSTRAP_ENABLED, RINDE_PLATFORM_BOOTSTRAP_NAME, RINDE_PLATFORM_BOOTSTRAP_EMAIL y RINDE_PLATFORM_BOOTSTRAP_PASSWORD. La contraseña inicial requiere de 12 a 72 caracteres. El arranque no actualiza ni reemplaza una cuenta existente; desactivar el alta inicial después de crearla. Nunca guardar credenciales reales en este README ni en los ejemplos.

El panel permite consultar empresas con filtros y paginación, suspender o restablecer su acceso, crear planes y retirarlos o publicarlos, consultar suscripciones y pagos simulados y revisar auditoría de empresas y catálogo. Cada cambio exige un motivo y guarda actor, fecha y estados anterior y posterior en la misma transacción del servicio. El registro de auditoría no tiene operaciones públicas de edición o borrado.

La suspensión administrativa se conserva por separado del estado comercial de la suscripción. Confirmar un pago no levanta una suspensión del dueño; restablecer el acceso no elimina una restricción por falta de pago. Los tokens de empresa se vuelven a contrastar con IAM. Trip devuelve 503 si no puede comprobar ese estado, en lugar de aceptar uno anterior del token.

Los precios y límites de los planes existentes no se editan. Para cambiar la oferta se crea otro plan y se retira el anterior, conservando las condiciones de las suscripciones que ya lo referencian. Los pagos continúan simulados: el panel no inicia cobros bancarios.

La auditoría de identidad está en IAM; la del catálogo, en core. Las consultas globales de suscripción obtienen los nombres por HTTP desde IAM, sin leer sus tablas. Las pruebas negativas cubren rutas y credenciales en ambos sentidos, y el script de despliegue prueba el flujo entre procesos.

## Librería publicada

Coordenadas de desarrollo: com.nexus:rinde-shared-kernel:0.2.0. El repositorio Maven es https://maven.pkg.github.com/nexus-upc/rinde-services. Para consumirlo fuera del reactor se necesita un servidor Maven con id github y credenciales de lectura de GitHub Packages configuradas localmente; no incluirlas en el repositorio. El job publica la librería y crea un proyecto consumidor con un repositorio Maven vacío, compila y ejecuta una clase que la usa y compara el JAR descargado con el publicado. Una versión publicada no se reutiliza para cambiar su contenido.