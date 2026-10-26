package com.nexus.rinde.iam.domain.model.commands;

/** Registra una empresa junto con su usuario administrador. */
public record RegisterTenantCommand(
    String tradeName,
    String ruc,
    String administratorFullName,
    String administratorEmail,
    String password) {}
