package com.nexus.rinde.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.web.util.UriUtils;

/**
 * Decodifica la ruta antes de aplicar reglas. Rechaza segmentos "." y ".." para que el gateway y el
 * servicio destino vean la misma ruta.
 */
public final class RequestPaths {

  private RequestPaths() {}

  /** Devuelve la ruta decodificada o vacío si tiene segmentos relativos o codificación inválida. */
  public static Optional<String> canonical(String rawPath) {
    String decoded;
    try {
      decoded = UriUtils.decode(rawPath, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
    if (decoded.contains("\\")) {
      return Optional.empty();
    }
    for (String segment : decoded.split("/", -1)) {
      if (".".equals(segment) || "..".equals(segment)) {
        return Optional.empty();
      }
    }
    return Optional.of(decoded);
  }
}
