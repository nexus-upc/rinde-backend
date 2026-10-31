# rinde-shared-kernel

Núcleo compartido de RINDE by NEXUS. Es una librería Java 21 con Spring Boot 3 que reúne las piezas comunes a todos los contextos: excepciones de dominio, el agregado auditable base, los eventos de integración, la seguridad por JWT y el manejo de errores REST.

## Contenido

Todas las clases están en el paquete `com.nexus.rinde.shared`:

- `domain/exceptions`: excepciones de negocio que el manejador REST traduce a respuestas HTTP (`ResourceNotFoundException`, `ConflictException`, `BusinessRuleException`, `ForbiddenOperationException` y `AuthenticationFailedException`).
- `domain/model`: `AuditableAbstractAggregateRoot`, base de los agregados con auditoría, e `IntegrationEvent` con `IdempotencyKey`.
- `infrastructure/security`: filtro `JwtAuthenticationFilter`, lector `JwtTokenReader`, `JwtClaims`, `CurrentUser`, `TenantContext` y `TenantStatusProvider`. Para la administración de Rinde, `PlatformPrincipal`, `PlatformTokenReader`, `PlatformAuthenticationFilter` y `PlatformAuditTrail`.
- `infrastructure/configuration` e `infrastructure/persistence/jpa`: reloj del sistema (`Clock`) y registro de entidades y repositorios de `com.nexus.rinde`.
- `interfaces/rest`: `CorrelationIdFilter`, `GlobalExceptionHandler` y `ProblemDetails`.

## Coordenadas

```xml
<groupId>com.nexus</groupId>
<artifactId>rinde-shared-kernel</artifactId>
<version>0.2.0</version>
```

## Uso en un servicio

Agregar la dependencia en el `pom.xml` del servicio:

```xml
<dependency>
  <groupId>com.nexus</groupId>
  <artifactId>rinde-shared-kernel</artifactId>
  <version>0.2.0</version>
</dependency>
```

La librería se publica en GitHub Packages. Para resolverla, declarar el repositorio en el `pom.xml` del servicio:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/nexus-upc/rinde-services</url>
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

Dentro del monorepo, el reactor compila la librería sin descargarla. Para usarla fuera del reactor sin credenciales de GitHub Packages, instalarla en el repositorio local con `mvnw.cmd -pl libraries/shared-kernel install` desde la raíz.

Para que la librería funcione, el servicio debe escanear el paquete `com.nexus.rinde` (las clases de `shared` son componentes de Spring) y definir la propiedad `rinde.security.jwt.secret`, que usa `JwtTokenReader`.

## Compilación y pruebas

```bash
./mvnw clean verify
```

## Versión

La versión 0.1.0 se extrajo de `rinde-backend` en su versión 0.1.0. El código del paquete `com.nexus.rinde.shared` es el mismo que en esa versión del monolito.

La versión 0.2.0, tag `shared-kernel/v0.2.0`, agrega la identidad de plataforma: `PlatformPrincipal`, `PlatformTokenReader`, `PlatformAuthenticationFilter` y `PlatformAuditTrail`, además de `PageResource` para respuestas paginadas. `JwtAuthenticationFilter` deja las rutas `/api/v1/platform/` al filtro de plataforma y responde 403 cuando la empresa del token tiene el acceso suspendido. Está publicada en GitHub Packages y no se modifica; un cambio nuevo requiere otra versión. La publica el workflow `publish-shared-kernel.yml`, que además la descarga desde un repositorio Maven vacío y la usa en un proyecto nuevo para comprobar que se puede consumir.
