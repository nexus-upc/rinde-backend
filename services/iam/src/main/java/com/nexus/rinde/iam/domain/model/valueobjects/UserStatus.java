package com.nexus.rinde.iam.domain.model.valueobjects;

/** Estado del usuario: invitado hasta que define su contraseña, luego activo, o deshabilitado. */
public enum UserStatus {
  INVITED,
  ACTIVE,
  DISABLED
}
