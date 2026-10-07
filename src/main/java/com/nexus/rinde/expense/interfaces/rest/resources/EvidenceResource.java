package com.nexus.rinde.expense.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/** Representación pública de un comprobante fotográfico. */
public record EvidenceResource(
    UUID id,
    String imageUrl,
    Long fileSizeBytes,
    Instant uploadedAt) {}
