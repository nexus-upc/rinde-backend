package com.nexus.rinde.iam.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class RucTest {

  @Test
  void acceptsElevenDigits() {
    assertThat(new Ruc("20123456789").number()).isEqualTo("20123456789");
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "2012345678", "201234567890", "2012345678A", "20 12345678"})
  void rejectsAnythingThatIsNotElevenDigits(String invalid) {
    assertThatThrownBy(() -> new Ruc(invalid)).isInstanceOf(BusinessRuleException.class);
  }
}
