package com.nexus.rinde.iam.application.internal;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Vigencia configurable de los enlaces de acceso: 48 h invitación, 1 h recuperación. */
@ConfigurationProperties("rinde.iam.tokens")
public record TokenValidityProperties(
    @DefaultValue("48") int invitationValidityHours,
    @DefaultValue("1") int passwordResetValidityHours,
    // TODO: el diseño no define la vigencia del enlace de verificación de la empresa; se asumió 48
    // h.
    @DefaultValue("48") int emailVerificationValidityHours) {

  public Duration invitationValidity() {
    return Duration.ofHours(invitationValidityHours);
  }

  public Duration passwordResetValidity() {
    return Duration.ofHours(passwordResetValidityHours);
  }

  public Duration emailVerificationValidity() {
    return Duration.ofHours(emailVerificationValidityHours);
  }
}
