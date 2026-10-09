package com.nexus.rinde.iam.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

  @Test
  void storesTheAddressInLowerCaseWithoutSurroundingSpaces() {
    assertThat(new Email("  Admin@Transportes.PE ").address()).isEqualTo("admin@transportes.pe");
  }

  @Test
  void twoEmailsDifferingOnlyInCaseAreEqual() {
    assertThat(new Email("A@b.com")).isEqualTo(new Email("a@B.com"));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "sin-arroba", "a@", "@b.com", "a@b", "a b@c.com"})
  void rejectsAnInvalidFormat(String invalid) {
    assertThatThrownBy(() -> new Email(invalid)).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void rejectsAnAddressLongerThan150Characters() {
    String tooLong = "a".repeat(140) + "@example.com";

    assertThatThrownBy(() -> new Email(tooLong)).isInstanceOf(BusinessRuleException.class);
  }
}
