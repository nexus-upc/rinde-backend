package com.nexus.rinde.expense.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/** Enlace temporal firmado para la subida de comprobantes fotográficos (PRN-06, QA-05). */
public record PresignedUrlResponseResource(
    @Schema(example = "https://storage.rinde.pe/evidences/upload/3c589391-d520?signature=xyz")
        String uploadUrl,
    @Schema(example = "https://storage.rinde.pe/evidences/3c589391-d520.jpg")
        String fileUrl,
    @Schema(example = "900")
        int expiresInSeconds) {}
