package com.nexus.rinde.iam.domain.model.valueobjects;

/** Para qué sirve un enlace de acceso. */
public enum TokenPurpose {
  INVITATION,
  PASSWORD_RESET,
  // TODO: EMAIL_VERIFICATION no está en el class diagram (se agregó para US08).
  // Actualizar el diagrama.
  EMAIL_VERIFICATION
}
