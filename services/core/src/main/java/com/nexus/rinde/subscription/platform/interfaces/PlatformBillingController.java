package com.nexus.rinde.subscription.platform.interfaces;

import com.nexus.rinde.shared.infrastructure.security.*;
import com.nexus.rinde.shared.interfaces.rest.PageResource;
import com.nexus.rinde.subscription.platform.application.PlatformPlanService;
import com.nexus.rinde.subscription.platform.infrastructure.PlatformBillingQueries;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/platform")
@PreAuthorize("hasAuthority('ROLE_PLATFORM_OWNER')")
public class PlatformBillingController {
  private final PlatformPlanService plans;
  private final PlatformBillingQueries queries;

  public PlatformBillingController(PlatformPlanService plans, PlatformBillingQueries queries) {
    this.plans = plans;
    this.queries = queries;
  }

  @GetMapping("/billing/overview")
  public PlatformBillingQueries.Overview overview() {
    return queries.overview();
  }

  @GetMapping("/plans")
  public PageResource<PlatformPlanService.PlanRow> plans(
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000000) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return plans.list(page, size);
  }

  @PostMapping("/plans")
  @ResponseStatus(HttpStatus.CREATED)
  public PlatformPlanService.PlanRow create(
      @Valid @RequestBody CreatePlan body, @AuthenticationPrincipal PlatformPrincipal principal) {
    return plans.create(
        body.name().trim(), body.monthlyPrice(), body.unitLimit(), body.reason().trim(), principal);
  }

  @PatchMapping("/plans/{id}/availability")
  public PlatformPlanService.PlanRow availability(
      @PathVariable UUID id,
      @Valid @RequestBody Availability body,
      @AuthenticationPrincipal PlatformPrincipal principal) {
    return plans.availability(id, body.active(), body.reason().trim(), principal);
  }

  @GetMapping("/subscriptions")
  public PageResource<PlatformBillingQueries.SubscriptionRow> subscriptions(
      @RequestParam(required = false) UUID tenantId,
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000000) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return queries.subscriptions(tenantId, page, size);
  }

  @GetMapping("/payments")
  public PageResource<PlatformBillingQueries.PaymentRow> payments(
      @RequestParam(required = false) UUID tenantId,
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000000) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return queries.payments(tenantId, page, size);
  }

  @GetMapping("/audit/billing")
  public PageResource<PlatformAuditTrail.Entry> audit(
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000000) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return plans.audit(page, size);
  }

  public record CreatePlan(
      @NotBlank @Size(max = 80) String name,
      @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal monthlyPrice,
      @NotNull @Min(1) @Max(100000) Integer unitLimit,
      @NotBlank @Size(min = 3, max = 500) String reason) {}

  public record Availability(
      @NotNull Boolean active, @NotBlank @Size(min = 3, max = 500) String reason) {}
}
