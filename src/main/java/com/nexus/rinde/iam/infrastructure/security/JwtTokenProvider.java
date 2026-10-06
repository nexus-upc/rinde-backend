package com.nexus.rinde.iam.infrastructure.security;

import com.nexus.rinde.iam.application.internal.outboundservices.AuthenticationTokenService;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.valueobjects.AuthenticationResult;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantStatus;
import com.nexus.rinde.shared.infrastructure.security.JwtClaims;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * Emite el token JWT con el usuario, su rol y la empresa. Es el único lugar donde se firma un
 * token; los demás contextos solo lo leen con JwtTokenReader.
 */
@Component
public class JwtTokenProvider implements AuthenticationTokenService {

  private final SecretKey key;
  private final Duration validity;
  private final Clock clock;

  public JwtTokenProvider(JwtProperties properties, Clock clock) {
    this.key = JwtClaims.signingKey(properties.secret());
    this.validity = Duration.ofHours(properties.expirationHours());
    this.clock = clock;
  }

  @Override
  public AuthenticationResult issueFor(User user, TenantStatus tenantStatus) {
    Instant now = clock.instant();
    Instant expiration = now.plus(validity);
    String token =
        Jwts.builder()
            .subject(user.getId().toString())
            .claim(JwtClaims.TENANT_ID, user.getTenantId().value().toString())
            .claim(JwtClaims.ROLE, user.getRole().name())
            .claim(JwtClaims.TENANT_STATUS, tenantStatus.name())
            .claim(JwtClaims.EMAIL, user.getEmail().address())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(key)
            .compact();
    return new AuthenticationResult(token, validity.toSeconds());
  }
}
