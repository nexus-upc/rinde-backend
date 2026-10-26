package com.nexus.rinde.iam.domain.model.commands;

/** Solicita el enlace para recuperar la contraseña del correo indicado. */
public record RequestPasswordResetCommand(String email) {}
