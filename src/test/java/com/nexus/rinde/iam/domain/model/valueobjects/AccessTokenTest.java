package com.nexus.rinde.iam.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccessTokenTest {

  private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");
  private final TenantId tenantId = new TenantId(UUID.randomUUID());

  @Test
  void issuesATokenThatStoresOnlyTheHashOfTheRawValue() {
    IssuedAccessToken issued =
        AccessToken.issue(tenantId, TokenPurpose.INVITATION, Duration.ofHours(48), NOW);

    assertThat(issued.rawValue()).isNotBlank();
    assertThat(issued.token().value()).isNotEqualTo(issued.rawValue());
    assertThat(issued.token().value()).isEqualTo(AccessToken.hash(issued.rawValue()));
    assertThat(issued.token().expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(48)));
    assertThat(issued.token().used()).isFalse();
  }

  @Test
  void rawValuesAreDifferentEachTime() {
    IssuedAccessToken first =
        AccessToken.issue(tenantId, TokenPurpose.PASSWORD_RESET, Duration.ofHours(1), NOW);
    IssuedAccessToken second =
        AccessToken.issue(tenantId, TokenPurpose.PASSWORD_RESET, Duration.ofHours(1), NOW);

    assertThat(first.rawValue()).isNotEqualTo(second.rawValue());
  }

  @Test
  void isUsableOnlyBeforeExpirationAndWhileUnused() {
    AccessToken token =
        AccessToken.issue(tenantId, TokenPurpose.PASSWORD_RESET, Duration.ofHours(1), NOW).token();

    assertThat(token.isUsable(NOW.plusSeconds(60))).isTrue();
    assertThat(token.isUsable(NOW.plus(Duration.ofHours(1)))).isFalse();
    assertThat(token.markUsed(NOW.plusSeconds(60)).isUsable(NOW.plusSeconds(61))).isFalse();
  }

  @Test
  void markUsedKeepsTheOtherData() {
    AccessToken token =
        AccessToken.issue(tenantId, TokenPurpose.INVITATION, Duration.ofHours(48), NOW).token();

    AccessToken used = token.markUsed(NOW.plusSeconds(5));

    assertThat(used.used()).isTrue();
    assertThat(used.usedAt()).isEqualTo(NOW.plusSeconds(5));
    assertThat(used.id()).isEqualTo(token.id());
    assertThat(used.value()).isEqualTo(token.value());
    assertThat(used.purpose()).isEqualTo(TokenPurpose.INVITATION);
  }

  @Test
  void hashIsDeterministicAndHexadecimal() {
    assertThat(AccessToken.hash("abc"))
        .isEqualTo(AccessToken.hash("abc"))
        .hasSize(64)
        .matches("[0-9a-f]+");
  }
}
