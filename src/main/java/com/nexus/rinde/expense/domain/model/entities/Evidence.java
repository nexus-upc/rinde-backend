package com.nexus.rinde.expense.domain.model.entities;

import com.nexus.rinde.expense.domain.model.aggregates.Expense;
import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Entidad comprobante/evidencia fotográfica adjunta a un gasto. */
@Entity
@Table(schema = "expense", name = "evidences")
public class Evidence {

  @Id private UUID id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "expense_id", nullable = false, updatable = false, unique = true)
  private Expense expense;

  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @Column(name = "file_size_bytes", nullable = false)
  private Long fileSizeBytes;

  @Column(name = "uploaded_at", nullable = false)
  private Instant uploadedAt;

  protected Evidence() {}

  public Evidence(Expense expense, UUID tenantId, String imageUrl, Long fileSizeBytes, Instant uploadedAt) {
    if (imageUrl == null || imageUrl.isBlank()) {
      throw new BusinessRuleException("La URL de la imagen del comprobante es obligatoria.");
    }
    this.id = UUID.randomUUID();
    this.expense = expense;
    this.tenantId = tenantId;
    this.imageUrl = imageUrl.trim();
    this.fileSizeBytes = (fileSizeBytes != null && fileSizeBytes > 0) ? fileSizeBytes : 1024L;
    this.uploadedAt = (uploadedAt != null) ? uploadedAt : Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public Long getFileSizeBytes() {
    return fileSizeBytes;
  }

  public Instant getUploadedAt() {
    return uploadedAt;
  }

  public void setExpense(Expense expense) {
    this.expense = expense;
  }
}
