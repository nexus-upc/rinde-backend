package com.nexus.rinde.expense.domain.model.aggregates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseCategory;
import com.nexus.rinde.expense.domain.model.valueobjects.ExpenseStatus;
import com.nexus.rinde.expense.domain.model.valueobjects.Money;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExpenseTest {

  @Test
  @DisplayName("Un gasto registrado puede ser aprobado")
  void shouldApproveRegisteredExpense() {
    Expense expense =
        Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            ExpenseCategory.FUEL,
            Money.of(new BigDecimal("120.00"), "PEN"),
            LocalDate.now(),
            "KEY-TEST-01",
            "https://storage.rinde.pe/recibo.jpg",
            1024L,
            Instant.now());

    assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REGISTERED);
    expense.approve(UUID.randomUUID(), Instant.now());
    assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
  }

  @Test
  @DisplayName("Un gasto registrado puede ser observado indicando motivo")
  void shouldObserveRegisteredExpenseWithReason() {
    Expense expense =
        Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            ExpenseCategory.TOLL,
            Money.of(new BigDecimal("15.50"), "PEN"),
            LocalDate.now(),
            "KEY-TEST-02",
            "https://storage.rinde.pe/recibo.jpg",
            1024L,
            Instant.now());

    expense.observe(UUID.randomUUID(), "Comprobante borroso", Instant.now());
    assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.OBSERVED);
    assertThat(expense.getObservationReason()).isEqualTo("Comprobante borroso");
  }

  @Test
  @DisplayName("No se puede observar un gasto sin motivo")
  void shouldRejectObservationWithoutReason() {
    Expense expense =
        Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            ExpenseCategory.TOLL,
            Money.of(new BigDecimal("15.50"), "PEN"),
            LocalDate.now(),
            "KEY-TEST-03",
            "https://storage.rinde.pe/recibo.jpg",
            1024L,
            Instant.now());

    assertThatThrownBy(() -> expense.observe(UUID.randomUUID(), null, Instant.now()))
        .isInstanceOf(BusinessRuleException.class);
    assertThatThrownBy(() -> expense.observe(UUID.randomUUID(), "   ", Instant.now()))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  @DisplayName("Un gasto bloqueado no puede ser modificado")
  void shouldNotModifyLockedExpense() {
    Expense expense =
        Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            ExpenseCategory.FOOD,
            Money.of(new BigDecimal("35.00"), "PEN"),
            LocalDate.now(),
            "KEY-TEST-04",
            null,
            null,
            Instant.now());

    expense.lock();
    assertThat(expense.isLocked()).isTrue();
    assertThatThrownBy(() -> expense.approve(UUID.randomUUID(), Instant.now()))
        .isInstanceOf(ConflictException.class);
    assertThatThrownBy(() -> expense.observe(UUID.randomUUID(), "Motivo", Instant.now()))
        .isInstanceOf(ConflictException.class);
  }
}
