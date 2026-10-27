package com.nexus.rinde.iam.platform.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/** Dueño de la plataforma, sin pertenencia a una empresa cliente. */
@Entity
@Table(schema = "iam", name = "platform_owners")
public class PlatformOwner {
  @Id private UUID id;

  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PlatformOwner() {}

  public PlatformOwner(String fullName, String email, String passwordHash, Instant now) {
    this.id = UUID.randomUUID();
    this.fullName = fullName.trim();
    this.email = email.trim().toLowerCase(Locale.ROOT);
    this.passwordHash = passwordHash;
    this.createdAt = now;
  }

  public UUID getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }
}
