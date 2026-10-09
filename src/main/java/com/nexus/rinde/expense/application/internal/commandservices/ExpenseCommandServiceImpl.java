package com.nexus.rinde.expense.application.internal.commandservices;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.domain.model.commands.AttachEvidenceCommand;
import com.nexus.rinde.expense.domain.model.commands.RegisterExpenseCommand;
import com.nexus.rinde.expense.domain.model.commands.UpdateExpenseStatusCommand;
import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseStatus;
import com.nexus.rinde.expense.domain.services.ExpenseCommandService;
import com.nexus.rinde.expense.domain.services.TripStatusService;
import com.nexus.rinde.expense.infrastructure.persistence.jpa.repositories.ExpenseRepository;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordina la creación, auditoría y evidencia de gastos con su persistencia. */
@Service
public class ExpenseCommandServiceImpl implements ExpenseCommandService {

  private final ExpenseRepository expenseRepository;
  private final TripStatusService tripStatusService;
  private final Clock clock;

  public ExpenseCommandServiceImpl(
      ExpenseRepository expenseRepository, TripStatusService tripStatusService, Clock clock) {
    this.expenseRepository = expenseRepository;
    this.tripStatusService = tripStatusService;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Expense handle(RegisterExpenseCommand command) {
    if (expenseRepository.existsByIdempotencyKey(command.idempotencyKey())) {
      throw new ConflictException("La clave de idempotencia ya fue utilizada.");
    }

    requireTripAcceptingExpenses(command.tenantId(), command.tripId());

    Expense expense =
        Expense.create(
            command.tenantId(),
            command.tripId(),
            command.driverId(),
            command.category(),
            command.amount(),
            command.expenseDate(),
            command.idempotencyKey(),
            command.imageUrl(),
            command.fileSizeBytes(),
            clock.instant());

    return expenseRepository.saveAndFlush(expense);
  }

  @Override
  @Transactional
  public Expense handle(UpdateExpenseStatusCommand command) {
    Expense expense =
        expenseRepository
            .findByIdAndTenantId(command.expenseId(), command.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("El gasto no existe."));

    if (command.status() == ExpenseStatus.APPROVED) {
      expense.approve(command.reviewerId(), clock.instant());
    } else if (command.status() == ExpenseStatus.OBSERVED) {
      expense.observe(command.reviewerId(), command.reason(), clock.instant());
    }

    return expenseRepository.saveAndFlush(expense);
  }

  @Override
  @Transactional
  public Expense handle(AttachEvidenceCommand command) {
    Expense expense =
        expenseRepository
            .findByIdAndTenantId(command.expenseId(), command.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("El gasto no existe."));

    expense.attachEvidence(command.imageUrl(), command.fileSizeBytes(), clock.instant());
    return expenseRepository.saveAndFlush(expense);
  }

  @Override
  @Transactional
  public java.util.List<Expense> handleSync(java.util.List<RegisterExpenseCommand> commands) {
    java.util.List<Expense> synchronizedExpenses = new java.util.ArrayList<>();
    for (RegisterExpenseCommand cmd : commands) {
      java.util.Optional<Expense> existing =
          expenseRepository.findByIdempotencyKey(cmd.idempotencyKey());
      if (existing.isPresent()) {
        synchronizedExpenses.add(existing.get());
      } else {
        requireTripAcceptingExpenses(cmd.tenantId(), cmd.tripId());
        Expense expense =
            Expense.create(
                cmd.tenantId(),
                cmd.tripId(),
                cmd.driverId(),
                cmd.category(),
                cmd.amount(),
                cmd.expenseDate(),
                cmd.idempotencyKey(),
                cmd.imageUrl(),
                cmd.fileSizeBytes(),
                clock.instant());
        synchronizedExpenses.add(expenseRepository.save(expense));
      }
    }
    expenseRepository.flush();
    return synchronizedExpenses;
  }

  /** Permite gastos en viajes iniciados o finalizados (estos últimos llegan por sincronización). */
  private void requireTripAcceptingExpenses(UUID tenantId, UUID tripId) {
    String status =
        tripStatusService
            .findTripStatus(tenantId, tripId)
            .orElseThrow(() -> new ResourceNotFoundException("El viaje no existe."));

    if ("SETTLED".equals(status)) {
      throw new ConflictException("El viaje ya fue liquidado.");
    }
    if (!"IN_ROUTE".equals(status) && !"FINISHED".equals(status)) {
      throw new ConflictException("El viaje todavía no ha iniciado.");
    }
  }
}
