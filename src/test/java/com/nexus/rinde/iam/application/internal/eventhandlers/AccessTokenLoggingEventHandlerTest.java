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

  @BeforeEach
  void attachAppender() {
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void detachAppender() {
    logger.detachAppender(appender);
  }

  private String lastMessage() {
    ILoggingEvent event = appender.list.get(appender.list.size() - 1);
    assertThat(event.getLevel()).isEqualTo(Level.INFO);
    return event.getFormattedMessage();
  }

  @Test
  void logsTheVerificationTokenAtInfoLevel() {
    handler.on(
        new TenantRegistered(
            UUID.randomUUID(),
            Instant.now(),
            UUID.randomUUID(),
            "Andes",
            "ana@andes.pe",
            "token-verificacion",
            Instant.now()));

    assertThat(lastMessage()).contains("ana@andes.pe").contains("token-verificacion");
  }

  @Test
  void logsTheInvitationToken() {
    handler.on(
        new UserInvited(
            UUID.randomUUID(),
            Instant.now(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Luis",
            "luis@andes.pe",
            "DRIVER",
            "token-invitacion",
            Instant.now()));

    assertThat(lastMessage()).contains("luis@andes.pe").contains("token-invitacion");
  }

  @Test
  void logsTheResetToken() {
    handler.on(
        new PasswordResetRequested(
            UUID.randomUUID(),
            Instant.now(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "ana@andes.pe",
            "token-recuperacion",
            Instant.now()));

    assertThat(lastMessage()).contains("ana@andes.pe").contains("token-recuperacion");
  }
}
