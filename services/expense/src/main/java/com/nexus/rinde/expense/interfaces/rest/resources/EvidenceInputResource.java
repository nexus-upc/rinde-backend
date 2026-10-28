package com.nexus.rinde.expense.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/** Datos de comprobante al registrar un gasto. */
public record EvidenceInputResource(
    @NotBlank(message = "La URL del comprobante es obligatoria")
    String imageUrl,
    Long fileSizeBytes) {}
