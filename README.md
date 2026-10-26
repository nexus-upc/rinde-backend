# rinde-shared-kernel

Núcleo compartido de RINDE by NEXUS. Es una librería Java 21 con Spring Boot 3 que reúne las piezas comunes a todos los contextos: excepciones de dominio, el agregado auditable base, los eventos de integración, la seguridad por JWT y el manejo de errores REST.

## Contenido

Todas las clases están en el paquete `com.nexus.rinde.shared`:

- `domain/exceptions`: excepciones de negocio que el manejador REST traduce a respuestas HTTP (`ResourceNotFoundException`, `ConflictException`, `BusinessRuleException`, `ForbiddenOperationException` y `AuthenticationFailedException`).
- `domain/model`: `AuditableAbstractAggregateRoot`, base de los agregados con auditoría, e `IntegrationEvent` con `IdempotencyKey`.
- `infrastructure/security`: filtro `JwtAuthenticationFilter`, lector `JwtTokenReader`, `JwtClaims`, `CurrentUser`, `TenantContext` y `TenantStatusProvider`.
- `infrastructure/configuration` e `infrastructure/persistence/jpa`: reloj del sistema (`Clock`) y registro de entidades y repositorios de `com.nexus.rinde`.
- `interfaces/rest`: `CorrelationIdFilter`, `GlobalExceptionHandler` y `ProblemDetails`.

## Coordenadas

```xml
<groupId>com.nexus</groupId>
<artifactId>rinde-shared-kernel</artifactId>
<version>0.1.0</version>
```

## Uso en un servicio

Agregar la dependencia en el `pom.xml` del servicio:

```xml
<dependency>
  <groupId>com.nexus</groupId>
  <artifactId>rinde-shared-kernel</artifactId>
  <version>0.1.0</version>
</dependency>
```

La librería se publica en GitHub Packages. Para resolverla, declarar el repositorio en el `pom.xml` del servicio:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/nexus-upc/rinde-shared-kernel</url>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
</repositories>
```

GitHub Packages exige autenticación también para leer. En `~/.m2/settings.xml` se define el servidor con el identificador `github`, usando un token personal con el permiso `read:packages`:

```xml
<servers>
  <server>
    <id>github</id>
    <username>USUARIO_GITHUB</username>
    <password>${env.GITHUB_TOKEN}</password>
  </server>
</servers>
```

Mientras el paquete no esté publicado, se puede instalar en el repositorio local con `mvnw.cmd clean install` dentro de esta carpeta.

Para que la librería funcione, el servicio debe escanear el paquete `com.nexus.rinde` (las clases de `shared` son componentes de Spring) y definir la propiedad `rinde.security.jwt.secret`, que usa `JwtTokenReader`.

## Compilación y pruebas

```bash
./mvnw clean verify
```

## Versión

La versión 0.1.0 se extrajo de `rinde-backend` en su versión 0.1.0. El código del paquete `com.nexus.rinde.shared` es el mismo que en esa versión del monolito.
