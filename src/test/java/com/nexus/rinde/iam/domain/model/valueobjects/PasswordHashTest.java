package com.nexus.rinde.iam.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import org.junit.jupiter.api.Test;

class PasswordHashTest {

  @Test
  void rejectsBlankValues() {
    assertThatThrownBy(() -> new PasswordHash(" ")).isInstanceOf(BusinessRuleException.class);
    assertThatThrownBy(() -> new PasswordHash(null)).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void toStringDoesNotExposeTheHash() {
    assertThat(new PasswordHash("$2a$10$secret").toString()).doesNotContain("secret");
  }
}
