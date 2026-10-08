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

`src/main/resources/application.yml` no se sube al repositorio. Créalo con este contenido y completa tu usuario, tu contraseña y un secreto de al menos 32 caracteres:

```yaml
spring:
  application:
    name: rinde
  datasource:
    url: jdbc:postgresql://localhost:5432/rinde
    username: TU_USUARIO
    password: TU_CONTRASENA
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
        type:
          preferred_instant_jdbc_type: TIMESTAMP
  # Cada bounded context declara su propio Flyway (esquema y migraciones propios).
  flyway:
    enabled: false

server:
  port: 8080

rinde:
  security:
    jwt:
      secret: UN_SECRETO_DE_AL_MENOS_32_CARACTERES
      expiration-hours: 8
  iam:
    tokens:
      invitation-validity-hours: 48
      password-reset-validity-hours: 1
      email-verification-validity-hours: 48

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health

logging:
  pattern:
    level: "%5p [%X{correlationId:-}]"
```

## Ejecutar

El webhook de pago simulado requiere `RINDE_BILLING_SIMULATION_WEBHOOK_SECRET` con al menos 32 caracteres. En PowerShell puedes generar una clave aleatoria para la sesión y después iniciar la aplicación en esa misma terminal:

```powershell
$secretBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($secretBytes)
$env:RINDE_BILLING_SIMULATION_WEBHOOK_SECRET = [BitConverter]::ToString($secretBytes).Replace('-', '')
$rng.Dispose()
```

Configura el mismo valor en el entorno de Postman que firme los webhooks. Mantén la clave en variables locales y no la guardes en `application.yml` ni en Git.

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
