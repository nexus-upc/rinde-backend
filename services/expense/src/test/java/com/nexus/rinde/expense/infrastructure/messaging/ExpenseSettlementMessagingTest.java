package com.nexus.rinde.expense.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseCategory;
import com.nexus.rinde.expense.domain.model.valueobjects.Money;
import com.nexus.rinde.expense.infrastructure.persistence.jpa.repositories.ExpenseRepository;
import com.nexus.rinde.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ExpenseSettlementMessagingTest extends AbstractIntegrationTest {

  @Autowired private ExpenseSettlementMessaging messaging;
  @Autowired private ExpenseRepository expenseRepository;

  @Test
  @DisplayName("Recepción idempotente de evento settlement.closed bloquea los gastos")
  void shouldLockExpensesOnSettlementClosedIdempotently() {
    UUID tenantId = UUID.randomUUID();
    UUID tripId = UUID.randomUUID();
    UUID driverId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();

    Expense expense =
        Expense.create(
            tenantId,
            tripId,
            driverId,
            ExpenseCategory.FOOD,
            Money.of(new BigDecimal("45.00"), "PEN"),
            LocalDate.now(),
            "KEY-AMQP-01",
            null,
            null,
            Instant.now());
    expenseRepository.save(expense);
    assertThat(expense.isLocked()).isFalse();

    ExpenseSettlementMessaging.SettlementClosedEvent event =
        new ExpenseSettlementMessaging.SettlementClosedEvent(
            eventId, Instant.now(), tenantId, tripId, UUID.randomUUID());

    // Primera recepción: procesa y bloquea
    messaging.receive(event, null);

    Expense locked = expenseRepository.findById(expense.getId()).orElseThrow();
    assertThat(locked.isLocked()).isTrue();

    // Segunda recepción (duplicado de red): debe ignorarse sin fallar
    messaging.receive(event, null);

    Expense stillLocked = expenseRepository.findById(expense.getId()).orElseThrow();
    assertThat(stillLocked.isLocked()).isTrue();
  }
}
