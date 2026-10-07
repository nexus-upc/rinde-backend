package com.nexus.rinde.expense.interfaces.rest.transform;

import com.nexus.rinde.expense.domain.model.commands.UpdateExpenseStatusCommand;
import com.nexus.rinde.expense.interfaces.rest.resources.UpdateExpenseStatusResource;
import java.util.UUID;

/** Convierte el request de aprobación u observación de gasto en su comando. */
public final class UpdateExpenseStatusCommandFromResourceAssembler {

  private UpdateExpenseStatusCommandFromResourceAssembler() {}

  public static UpdateExpenseStatusCommand toCommand(
      UUID tenantId, UUID expenseId, UUID reviewerId, UpdateExpenseStatusResource resource) {
    return new UpdateExpenseStatusCommand(
        tenantId,
        expenseId,
        reviewerId,
        resource.status(),
        resource.reason());
  }
}
