package com.nexus.rinde.iam.domain.model.commands;

/** Define la contraseña usando un enlace de invitación o de recuperación. */
public record DefinePasswordCommand(String token, String password) {}
