package com.nexus.rinde.iam.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuración del token JWT: secreto de firma y horas de vigencia. */
@ConfigurationProperties("rinde.security.jwt")
public record JwtProperties(String secret, @DefaultValue("8") int expirationHours) {}
