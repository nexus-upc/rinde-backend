package com.nexus.rinde.notification.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class RetryPolicyTest {

  private final RetryPolicy policy =
      new RetryPolicy(
          List.of(
              Duration.ofMinutes(1),
              Duration.ofMinutes(2),
              Duration.ofMinutes(4),
              Duration.ofMinutes(8),
              Duration.ofMinutes(16)));

  @Test
  void waitsGrowFromOneToSixteenMinutesForRetriesOneToFive() {
    assertThat(policy.waitBeforeNextRetry(0)).contains(Duration.ofMinutes(1));
    assertThat(policy.waitBeforeNextRetry(1)).contains(Duration.ofMinutes(2));
    assertThat(policy.waitBeforeNextRetry(2)).contains(Duration.ofMinutes(4));
    assertThat(policy.waitBeforeNextRetry(3)).contains(Duration.ofMinutes(8));
    assertThat(policy.waitBeforeNextRetry(4)).contains(Duration.ofMinutes(16));
  }

  @Test
  void hasNoSixthRetry() {
    assertThat(policy.waitBeforeNextRetry(5)).isEmpty();
    assertThat(policy.waitBeforeNextRetry(9)).isEmpty();
  }

  @Test
  void rejectsNegativeRetryCount() {
    assertThatThrownBy(() -> policy.waitBeforeNextRetry(-1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsNonPositiveDelays() {
    assertThatThrownBy(() -> new RetryPolicy(List.of(Duration.ZERO)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
