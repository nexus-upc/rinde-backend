package com.nexus.rinde.iam.platform.application;

import com.nexus.rinde.iam.platform.domain.PlatformOwner;
import com.nexus.rinde.iam.platform.infrastructure.PlatformOwnerRepository;
import com.nexus.rinde.shared.domain.exceptions.AuthenticationFailedException;
import com.nexus.rinde.shared.infrastructure.security.*;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlatformIdentityService {
  private final PlatformOwnerRepository owners;
  private final PasswordEncoder encoder;
  private final Clock clock;
  private final String secret;
  private final String dummy;

  public PlatformIdentityService(
      PlatformOwnerRepository owners,
      PasswordEncoder encoder,
      Clock clock,
      @Value("${rinde.platform.jwt.secret:}") String secret) {
    this.owners = owners;
    this.encoder = encoder;
    this.clock = clock;
    this.secret = secret;
    this.dummy = encoder.encode("platform-timing-equalizer");
  }

  @Transactional(readOnly = true)
  public String signIn(String email, String password) {
    if (secret.isBlank())
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "El acceso de plataforma no está configurado.");
    PlatformOwner owner = owners.findByEmailIgnoreCase(email.trim()).orElse(null);
    boolean valid = encoder.matches(password, owner == null ? dummy : owner.getPasswordHash());
    if (owner == null || !valid)
      throw new AuthenticationFailedException("El correo o la contraseña son incorrectos.");
    var now = clock.instant();
    return Jwts.builder()
        .subject(owner.getId().toString())
        .issuer(PlatformTokenReader.ISSUER)
        .audience()
        .add(PlatformTokenReader.AUDIENCE)
        .and()
        .claim("scope", "PLATFORM")
        .claim("role", "PLATFORM_OWNER")
        .claim("email", owner.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(900)))
        .signWith(JwtClaims.signingKey(secret))
        .compact();
  }

  @Transactional(readOnly = true)
  public Profile profile(UUID id) {
    PlatformOwner owner =
        owners
            .findById(id)
            .orElseThrow(
                () -> new AuthenticationFailedException("La identidad de plataforma no existe."));
    return new Profile(owner.getId(), owner.getFullName(), owner.getEmail(), "PLATFORM_OWNER");
  }

  public record Profile(UUID id, String fullName, String email, String role) {}
}
