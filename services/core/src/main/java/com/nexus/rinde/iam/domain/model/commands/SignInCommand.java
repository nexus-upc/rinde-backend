package com.nexus.rinde.iam.domain.model.commands;

/** Inicia sesión con correo y contraseña. */
public record SignInCommand(String email, String password) {}
