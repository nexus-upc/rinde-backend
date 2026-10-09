package com.nexus.rinde.shared.domain.model.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IntegrationEventTest {

  record SampleEvent(UUID eventId, Instant occurredAt, UUID tenantId) implements IntegrationEvent {
    @Override
    public String type() {
      return "SampleEvent";
    }
  }

  @Test
  void idempotencyKeyDefaultsToTheEventId() {
    UUID eventId = UUID.randomUUID();
    SampleEvent event = new SampleEvent(eventId, Instant.now(), UUID.randomUUID());

    assertThat(event.idempotencyKey()).isEqualTo(eventId.toString());
    assertThat(event.type()).isEqualTo("SampleEvent");
  }
}
