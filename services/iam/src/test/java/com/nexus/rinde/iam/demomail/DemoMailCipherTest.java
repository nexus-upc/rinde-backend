package com.nexus.rinde.iam.demomail;

import static org.assertj.core.api.Assertions.*;

import com.nexus.rinde.iam.demomail.infrastructure.DemoMailCipher;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DemoMailCipherTest {
  @Test
  void encryptsWithRandomNoncesAndAuthenticatesTheMessageContext() {
    var cipher = new DemoMailCipher("a".repeat(64), new MockEnvironment());
    String first = cipher.encrypt("opaque-demo-token", "message-one");
    assertThat(first)
        .doesNotContain("opaque-demo-token")
        .isNotEqualTo(cipher.encrypt("opaque-demo-token", "message-one"));
    assertThat(cipher.decrypt(first, "message-one")).isEqualTo("opaque-demo-token");
    assertThatThrownBy(() -> cipher.decrypt(first, "message-two"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void requiresAKeyAndRefusesProductionProfiles() {
    assertThatThrownBy(() -> new DemoMailCipher("", new MockEnvironment()))
        .isInstanceOf(IllegalStateException.class);
    MockEnvironment production = new MockEnvironment();
    production.setActiveProfiles("production");
    assertThatThrownBy(() -> new DemoMailCipher("a".repeat(64), production))
        .isInstanceOf(IllegalStateException.class);
  }
}
