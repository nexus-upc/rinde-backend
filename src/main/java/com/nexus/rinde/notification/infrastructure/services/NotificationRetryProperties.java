package com.nexus.rinde.notification.infrastructure.services;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Esperas en minutos antes de cada reintento: 1, 2, 4, 8 y 16 por defecto. */
@ConfigurationProperties("rinde.notification.retry")
public record NotificationRetryProperties(
    @DefaultValue({"1", "2", "4", "8", "16"}) List<Long> delaysMinutes) {}
