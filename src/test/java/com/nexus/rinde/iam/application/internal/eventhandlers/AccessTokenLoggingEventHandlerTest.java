package com.nexus.rinde.iam.application.internal.eventhandlers;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.nexus.rinde.iam.domain.model.events.PasswordResetRequested;
import com.nexus.rinde.iam.domain.model.events.TenantRegistered;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class AccessTokenLoggingEventHandlerTest {

  private final AccessTokenLoggingEventHandler handler = new AccessTokenLoggingEventHandler();
  private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
  private final Logger logger =
      (Logger) LoggerFactory.getLogger(AccessTokenLoggingEventHandler.class);
  private Level levelBefore;

  @BeforeEach
  void attachAppender() {
    levelBefore = logger.getLevel();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void detachAppenderAndRestoreLevel() {
    logger.detachAppender(appender);
    logger.setLevel(levelBefore);
  }

  private String messageAt(Level level) {
    List<String> messages =
        appender.list.stream()
            .filter(event -> event.getLevel() == level)
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
    assertThat(messages).hasSize(1);
    return messages.get(0);
  }

  private void assertInfoMasked(String email, String maskedToken, String fullToken) {
    String info = messageAt(Level.INFO);
    assertThat(info).contains(email).contains(maskedToken).doesNotContain(fullToken);
  }

  private TenantRegistered tenantRegistered(String verificationToken) {
    return new TenantRegistered(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        "Andes",
        "ana@andes.pe",
        verificationToken,
        Instant.now());
  }

  private UserInvited userInvited(String invitationToken) {
    return new UserInvited(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Luis",
        "luis@andes.pe",
        "DRIVER",
        invitationToken,
        Instant.now());
  }

  private PasswordResetRequested passwordResetRequested(String resetToken) {
    return new PasswordResetRequested(
        UUID.randomUUID(),
        Instant.now(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "ana@andes.pe",
        resetToken,
        Instant.now());
  }

  @Test
  void infoMasksTheVerificationToken() {
    logger.setLevel(Level.INFO);

    handler.on(tenantRegistered("token-verificacion"));

    assertThat(appender.list).hasSize(1);
    assertInfoMasked("ana@andes.pe", "toke****", "token-verificacion");
  }

  @Test
  void debugLogsTheFullVerificationToken() {
    logger.setLevel(Level.DEBUG);

    handler.on(tenantRegistered("token-verificacion"));

    assertInfoMasked("ana@andes.pe", "toke****", "token-verificacion");
    assertThat(messageAt(Level.DEBUG))
        .contains("ana@andes.pe")
        .contains("token-verificacion");
  }

  @Test
  void infoMasksTheInvitationToken() {
    logger.setLevel(Level.INFO);

    handler.on(userInvited("token-invitacion"));

    assertThat(appender.list).hasSize(1);
    assertInfoMasked("luis@andes.pe", "toke****", "token-invitacion");
  }

  @Test
  void debugLogsTheFullInvitationToken() {
    logger.setLevel(Level.DEBUG);

    handler.on(userInvited("token-invitacion"));

    assertInfoMasked("luis@andes.pe", "toke****", "token-invitacion");
    assertThat(messageAt(Level.DEBUG))
        .contains("luis@andes.pe")
        .contains("token-invitacion");
  }

  @Test
  void infoMasksTheResetToken() {
    logger.setLevel(Level.INFO);

    handler.on(passwordResetRequested("token-recuperacion"));

    assertThat(appender.list).hasSize(1);
    assertInfoMasked("ana@andes.pe", "toke****", "token-recuperacion");
  }

  @Test
  void debugLogsTheFullResetToken() {
    logger.setLevel(Level.DEBUG);

    handler.on(passwordResetRequested("token-recuperacion"));

    assertInfoMasked("ana@andes.pe", "toke****", "token-recuperacion");
    assertThat(messageAt(Level.DEBUG))
        .contains("ana@andes.pe")
        .contains("token-recuperacion");
  }

  @Test
  void shortTokenIsFullyHidden() {
    logger.setLevel(Level.INFO);

    handler.on(userInvited("a1b2"));

    assertThat(messageAt(Level.INFO))
        .contains("luis@andes.pe")
        .contains("****")
        .doesNotContain("a1b2");
  }

  @Test
  void nullTokenDoesNotFail() {
    logger.setLevel(Level.INFO);

    handler.on(userInvited(null));

    assertThat(messageAt(Level.INFO)).contains("luis@andes.pe").contains("null");
  }
}
