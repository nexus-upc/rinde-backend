package com.nexus.rinde.iam.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.rinde.iam.application.internal.TokenValidityProperties;
import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.commands.InviteUserCommand;
import com.nexus.rinde.iam.domain.model.commands.UpdateUserCommand;
import com.nexus.rinde.iam.domain.model.events.UserInvited;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import com.nexus.rinde.iam.domain.model.valueobjects.Role;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import com.nexus.rinde.iam.domain.model.valueobjects.TokenPurpose;
import com.nexus.rinde.iam.domain.model.valueobjects.UserId;
import com.nexus.rinde.iam.domain.model.valueobjects.UserStatus;
import com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import com.nexus.rinde.shared.domain.exceptions.ConflictException;
import com.nexus.rinde.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

  private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");

  @Mock private UserRepository userRepository;

  private UserCommandServiceImpl service;
  private final TenantId tenantId = new TenantId(UUID.randomUUID());

  @BeforeEach
  void setUp() {
    service =
        new UserCommandServiceImpl(
            userRepository,
            new TokenValidityProperties(48, 1, 48),
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private User activeUser(TenantId owner, String email) {
    return User.createAdministrator(
        owner, "Nombre " + email, new Email(email), new PasswordHash("h"));
  }

  @Test
  void inviteCreatesAnInvitedUserWithA48HourLink() {
    when(userRepository.saveAndFlush(any(User.class))).thenAnswer(call -> call.getArgument(0));

    User user =
        service.handle(
            new InviteUserCommand(tenantId, "Luis Quispe", "Luis@Andes.pe", Role.DRIVER));

    assertThat(user.getStatus()).isEqualTo(UserStatus.INVITED);
    assertThat(user.getEmail().address()).isEqualTo("luis@andes.pe");
    assertThat(user.getRole()).isEqualTo(Role.DRIVER);
    assertThat(user.getTenantId()).isEqualTo(tenantId);
    assertThat(user.getAccessTokens().get(0).purpose()).isEqualTo(TokenPurpose.INVITATION);
    assertThat(user.getAccessTokens().get(0).expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(48)));
    assertThat(user.pendingDomainEvents()).hasAtLeastOneElementOfType(UserInvited.class);
  }

  @Test
  void inviteRejectsAnEmailAlreadyRegistered() {
    when(userRepository.existsByEmail(new Email("luis@andes.pe"))).thenReturn(true);

    assertThatThrownBy(
            () ->
                service.handle(
                    new InviteUserCommand(tenantId, "Luis", "luis@andes.pe", Role.DRIVER)))
        .isInstanceOf(ConflictException.class);
    verify(userRepository, never()).saveAndFlush(any());
  }

  @Test
  void inviteTranslatesADatabaseUniqueViolationIntoAConflict() {
    when(userRepository.saveAndFlush(any()))
        .thenThrow(new DataIntegrityViolationException("duplicado"));

    assertThatThrownBy(
            () ->
                service.handle(
                    new InviteUserCommand(tenantId, "Luis", "luis@andes.pe", Role.DRIVER)))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void updateChangesTheRole() {
    User user = activeUser(tenantId, "ana@andes.pe");
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    User updated =
        service.handle(
            new UpdateUserCommand(tenantId, user.getUserId(), Role.OPERATIONS_MANAGER, false));

    assertThat(updated.getRole()).isEqualTo(Role.OPERATIONS_MANAGER);
    assertThat(updated.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  void updateDisablesTheUser() {
    User user = activeUser(tenantId, "ana@andes.pe");
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    User updated = service.handle(new UpdateUserCommand(tenantId, user.getUserId(), null, true));

    assertThat(updated.getStatus()).isEqualTo(UserStatus.DISABLED);
  }

  @Test
  void updateCanChangeTheRoleAndDisableInOneOperation() {
    User user = activeUser(tenantId, "ana@andes.pe");
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    User updated =
        service.handle(new UpdateUserCommand(tenantId, user.getUserId(), Role.DRIVER, true));

    assertThat(updated.getRole()).isEqualTo(Role.DRIVER);
    assertThat(updated.getStatus()).isEqualTo(UserStatus.DISABLED);
  }

  @Test
  void updateOfAUserFromAnotherTenantRespondsAsNotFound() {
    User outsider = activeUser(new TenantId(UUID.randomUUID()), "otro@otra.pe");
    when(userRepository.findById(outsider.getId())).thenReturn(Optional.of(outsider));

    assertThatThrownBy(
            () ->
                service.handle(
                    new UpdateUserCommand(tenantId, outsider.getUserId(), Role.DRIVER, false)))
        .isInstanceOf(ResourceNotFoundException.class);
    assertThat(outsider.getRole()).isEqualTo(Role.ADMINISTRATOR);
    verify(userRepository, never()).save(any());
  }

  @Test
  void updateOfAnUnknownUserRespondsAsNotFound() {
    UUID unknown = UUID.randomUUID();
    when(userRepository.findById(unknown)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.handle(new UpdateUserCommand(tenantId, new UserId(unknown), null, true)))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateRequiresAChange() {
    User user = activeUser(tenantId, "ana@andes.pe");

    assertThatThrownBy(
            () -> service.handle(new UpdateUserCommand(tenantId, user.getUserId(), null, false)))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void updateOfADisabledUserCannotChangeTheRole() {
    User user = activeUser(tenantId, "ana@andes.pe");
    user.disable();
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    assertThatThrownBy(
            () ->
                service.handle(
                    new UpdateUserCommand(tenantId, user.getUserId(), Role.DRIVER, false)))
        .isInstanceOf(BusinessRuleException.class);
  }
}
