package com.nexus.rinde.notification.domain.model.valueobjects;

import java.util.UUID;

/** Mensaje de correo operativo que Notifications entrega mediante un adaptador. */
public record EmailMessage(
    UUID eventId, UUID tenantId, String recipient, String subject, String body) {}
