package com.nexus.rinde.shared.domain.model.aggregates;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuditableAbstractAggregateRootTest {

  record SampleEvent(String name) {}

  static class SampleAggregate extends AuditableAbstractAggregateRoot {
    void doSomething() {
      registerEvent(new SampleEvent("done"));
    }
  }

  @Test
  void assignsIdOnCreationAndIsNewUntilPersisted() {
    SampleAggregate aggregate = new SampleAggregate();

    assertThat(aggregate.getId()).isNotNull();
    assertThat(aggregate.isNew()).isTrue();

    aggregate.markNotNew();

    assertThat(aggregate.isNew()).isFalse();
  }

  @Test
  void setsAuditDatesOnPersistAndRefreshesUpdatedAtOnUpdate() throws InterruptedException {
    SampleAggregate aggregate = new SampleAggregate();

    aggregate.onCreate();
    assertThat(aggregate.getCreatedAt()).isNotNull().isEqualTo(aggregate.getUpdatedAt());

    Thread.sleep(5);
    aggregate.onUpdate();

    assertThat(aggregate.getUpdatedAt()).isAfter(aggregate.getCreatedAt());
  }

  @Test
  void keepsRegisteredEventsUntilCleared() {
    SampleAggregate aggregate = new SampleAggregate();

    aggregate.doSomething();

    assertThat(aggregate.pendingDomainEvents()).containsExactly(new SampleEvent("done"));

    aggregate.clearDomainEvents();

    assertThat(aggregate.pendingDomainEvents()).isEmpty();
  }

  @Test
  void eachAggregateGetsADifferentId() {
    assertThat(new SampleAggregate().getId()).isNotEqualTo(new SampleAggregate().getId());
  }
}
