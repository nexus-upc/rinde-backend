# RINDE Backend

Backend de RINDE by NEXUS: gestión de viajes, gastos, liquidaciones, flota e incidencias para pymes de transporte de carga.

Es un monolito modular en Spring Boot. Cada bounded context tiene su paquete (`iam`, `trip`, `expense`, `settlement`, `fleet`, `incident`, `subscription`, `notification`, `dashboard`) y lo común está en `shared`.

## Requisitos

- JDK 21
- PostgreSQL 16 o superior

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
