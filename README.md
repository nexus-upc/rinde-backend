# rinde-gateway

Puerta de enlace de RINDE by NEXUS. Recibe las peticiones de `rinde-web` en el puerto 8000, valida el token de acceso y las reenvía a `rinde-iam-service`, `rinde-trip-service` o al monolito. Está hecha con Spring Cloud Gateway (WebFlux), Java 21 y Maven.

El gateway no tiene lógica de negocio. Los servicios vuelven a validar el token, así que el encabezado `Authorization` se reenvía sin cambios.

## Rutas

| Ruta | Destino | Variable |
| --- | --- | --- |
| `/api/v1/auth/**`, `/api/v1/tenants/**`, `/api/v1/users/**` | IAM (`/api/v1/...`) | `IAM_SERVICE_URL` |
| `/api/v1/trips/{id}/expenses/**`, `/api/v1/trips/{id}/settlement/**` | Monolito (Expense y Settlement) | `MONOLITH_URL` |
| `/api/v1/trips`, `/api/v1/trips/**` (resto de viajes) | Trip (`/api/v1/trips/...`) | `TRIP_SERVICE_URL` |
| `/api/v1/**` (resto de rutas) | Monolito | `MONOLITH_URL` |
| `/v3/api-docs/iam` | IAM (`/v3/api-docs`) | `IAM_SERVICE_URL` |
| `/v3/api-docs/trip` | Trip (`/v3/api-docs`) | `TRIP_SERVICE_URL` |
| `/v3/api-docs/monolith` | Monolito (`/v3/api-docs`) | `MONOLITH_URL` |
| `/swagger-ui.html` | Swagger UI con los documentos de IAM, Trip y del monolito | |
| `/api/internal/**` | Ninguno. Responde 404 y nunca se reenvía | |
| `/actuator/health` | Estado del propio gateway | |

Las rutas se declaran en el orden del `order` de `application.yml`, y gana la primera que coincide. Las rutas de IAM y las de gastos y liquidación de viajes van antes que los comodines, para que las subrutas de viajes que siguen en el monolito no pasen a Trip.

## Rutas sin token

Estas peticiones pasan sin `Authorization`:

- `POST /api/v1/tenants` (registro de empresa).
- `POST /api/v1/tenants/{id}/verification`.
- `POST /api/v1/auth/sign-in`.
- `POST /api/v1/auth/password-reset`.
- `POST /api/v1/auth/password`.
- Cualquier método sobre `/api/v1/billing/webhooks/**`. El monolito valida la firma del webhook.
- `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/webjars/**` y `/actuator/health`.
- Las peticiones preflight CORS (`OPTIONS`).

El resto exige `Authorization: Bearer <jwt>`. Si el token falta, tiene firma distinta o está vencido, el gateway responde 401 con un cuerpo RFC 7807 y el mensaje "Token ausente o inválido.".

## Comportamiento general

- Si la ruta contiene segmentos "." o "..", después de decodificarla, el gateway responde 400 y no reenvía la petición.
- Las respuestas de las rutas de negocio llevan `X-Correlation-Id`. Si el cliente envía un valor válido, se reutiliza y se propaga a los servicios. Si falta o no cumple el formato, el gateway genera uno nuevo.
- CORS se configura solo con `CORS_ALLOWED_ORIGINS`. Se aceptan los métodos GET, POST, PUT, PATCH, DELETE y OPTIONS, y los encabezados `Authorization`, `Content-Type` y `X-Correlation-Id`.

## Variables de entorno

Las variables se leen del archivo `.env` de la raíz, que no se sube a Git. Copia `.env.example` como `.env` y completa los valores.

| Variable | Obligatoria | Valor por defecto | Descripción |
| --- | --- | --- | --- |
| `JWT_SECRET` | Sí | Ninguno | Secreto HS256 de al menos 32 caracteres. Debe ser el mismo que usan IAM y el monolito. |
| `IAM_SERVICE_URL` | No | `http://localhost:8081` | Dirección de `rinde-iam-service`. |
| `TRIP_SERVICE_URL` | No | `http://localhost:8082` | Dirección de `rinde-trip-service`. |
| `MONOLITH_URL` | No | `http://localhost:8080` | Dirección del monolito `rinde-backend`. |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:4200` | Orígenes permitidos, separados por comas. |

## Puesta en marcha

El gateway depende de otros servicios para responder a las rutas de negocio. El orden recomendado es el siguiente:

1. Iniciar `rinde-iam-service` en el puerto 8081 con `.\mvnw.cmd spring-boot:run`. Requiere PostgreSQL con la base `rinde_iam`.
2. Iniciar `rinde-trip-service` en el puerto 8082 con `.\mvnw.cmd spring-boot:run`. Requiere PostgreSQL con la base `rinde_trip` y RabbitMQ.
3. Iniciar el monolito `rinde-backend` en el puerto 8080 con `.\mvnw.cmd spring-boot:run`.
4. Iniciar el gateway en el puerto 8000 con `.\mvnw.cmd spring-boot:run`.

Para comprobar que el gateway está activo, consultar `http://localhost:8000/actuator/health`. Para revisar la documentación, abrir `http://localhost:8000/swagger-ui.html`.

## Pruebas

```
.\mvnw.cmd clean verify
```

Las pruebas no necesitan IAM ni el monolito en ejecución. Usan tres servidores WireMock que hacen de servicios (IAM, Trip y el monolito) y un secreto JWT de prueba, distinto de cualquier entorno real. Cubren el enrutamiento a Trip y al monolito, la validación del token, las rutas públicas, `/api/internal`, CORS y la propagación de `X-Correlation-Id`.
