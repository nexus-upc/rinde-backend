package com.nexus.rinde.iam.platform.infrastructure;

import com.nexus.rinde.iam.platform.domain.PlatformOwner;
import com.nexus.rinde.shared.infrastructure.security.JwtClaims;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Alta inicial optativa desde secretos del entorno; no modifica una cuenta ya creada. */
@Component
@ConditionalOnProperty(name = "rinde.platform.bootstrap.enabled", havingValue = "true")
public class PlatformBootstrap implements ApplicationRunner {
  private final PlatformOwnerRepository owners;
  private final PasswordEncoder encoder;
  private final Clock clock;
  private final String name, email, password, secret;

  public PlatformBootstrap(
      PlatformOwnerRepository owners,
      PasswordEncoder encoder,
      Clock clock,
      @Value("${rinde.platform.bootstrap.name:}") String name,
      @Value("${rinde.platform.bootstrap.email:}") String email,
      @Value("${rinde.platform.bootstrap.password:}") String password,
      @Value("${rinde.platform.jwt.secret:}") String secret) {
    this.owners = owners;
    this.encoder = encoder;
    this.clock = clock;
    this.name = name;
    this.email = email;
    this.password = password;
    this.secret = secret;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    JwtClaims.signingKey(secret);
    if (name.isBlank()
        || name.length() > 150
        || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
        || email.length() > 150
        || password.length() < 12
        || password.length() > 72) {
      throw new IllegalStateException(
          "Configure nombre, correo y contraseña inicial de plataforma (12 a 72 caracteres).");
    }
    if (owners.findByEmailIgnoreCase(email.trim()).isEmpty()) {
      owners.save(new PlatformOwner(name, email, encoder.encode(password), clock.instant()));
    }
  }
}
