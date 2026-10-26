package com.nexus.rinde.trip.interfaces.internal;

import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import com.nexus.rinde.trip.domain.model.aggregates.Trip;
import com.nexus.rinde.trip.domain.services.TripInternalQueryService;
import com.nexus.rinde.trip.interfaces.internal.resources.InternalTripResource;
import com.nexus.rinde.trip.interfaces.internal.resources.TripStatusResource;
import com.nexus.rinde.trip.interfaces.internal.transform.InternalTripResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultas de viajes que usan Expense, Dashboard y Settlement por HTTP. Reemplazan la fachada
 * TripContextFacade del monolito. No las enruta el gateway.
 */
@RestController
@RequestMapping(
    value = "/api/internal/v1/trips/tenants/{tenantId}",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Internal", description = "Consultas entre servicios de RINDE (no públicas)")
@SecurityRequirements
public class InternalTripsController {

  private final TripInternalQueryService tripInternalQueryService;

  public InternalTripsController(TripInternalQueryService tripInternalQueryService) {
    this.tripInternalQueryService = tripInternalQueryService;
  }

  @GetMapping
  @Operation(summary = "Viajes de la empresa")
  public List<InternalTripResource> list(@PathVariable UUID tenantId) {
    return tripInternalQueryService.findByTenant(tenantId).stream()
        .map(InternalTripResourceAssembler::toResource)
        .toList();
  }

  @GetMapping("/{tripId}")
  @Operation(summary = "Resumen de un viaje de la empresa")
  public InternalTripResource detail(@PathVariable UUID tenantId, @PathVariable UUID tripId) {
    return InternalTripResourceAssembler.toResource(find(tenantId, tripId));
  }

  @GetMapping("/{tripId}/status")
  @Operation(summary = "Estado actual de un viaje de la empresa")
  public TripStatusResource status(@PathVariable UUID tenantId, @PathVariable UUID tripId) {
    return InternalTripResourceAssembler.toStatusResource(find(tenantId, tripId));
  }

  private Trip find(UUID tenantId, UUID tripId) {
    return tripInternalQueryService
        .findByTripAndTenant(tenantId, tripId)
        .orElseThrow(() -> new ResourceNotFoundException("No existe el viaje en la empresa."));
  }
}
