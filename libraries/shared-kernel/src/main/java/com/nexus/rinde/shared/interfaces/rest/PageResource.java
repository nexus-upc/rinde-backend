package com.nexus.rinde.shared.interfaces.rest;

import java.util.List;
import org.springframework.data.domain.Page;

/** Contrato de paginación estable para las consultas administrativas. */
public record PageResource<T>(List<T> items, int page, int size, long total) {
  public static <T> PageResource<T> from(Page<T> page) {
    return new PageResource<>(
        List.copyOf(page.getContent()), page.getNumber(), page.getSize(), page.getTotalElements());
  }
}
