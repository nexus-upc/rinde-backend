# rinde-fleet-service

Servicio de Fleet & Maintenance de RINDE by NEXUS. Tarea S2-04, a cargo de Andree.

Esta carpeta es un esqueleto: compila y arranca, pero todavía no tiene el código del contexto. Ya trae el pom.xml con la librería rinde-shared-kernel 0.2.0, el wrapper de Maven, el Dockerfile, el application.yml, la configuración de OpenAPI y Flyway para el esquema `fleet`.

## Pasos pendientes

1. Copiar el paquete `fleet` de `services/core` en `src/main/java/com/nexus/rinde/fleet`, sus migraciones en `src/main/resources/db/migration/fleet` y sus pruebas y archivos .feature. No borrar nada de core.
2. Reemplazar lo que el contexto usa de otros contextos por clientes HTTP con DTO propios o por eventos de RabbitMQ. El detalle está en el documento de tareas del Avance 3.
3. Completar este README con los endpoints, los contratos internos y los eventos.

## Ejecución

Requiere Java 21, PostgreSQL y RabbitMQ. Crear las bases `rinde_fleet` y `rinde_fleet_test`, copiar `.env.example` como `.env` y completar los valores.

```
mvnw.cmd spring-boot:run
mvnw.cmd clean verify
```

Puerto: 8083. Swagger: http://localhost:8083/swagger-ui.html.
