package com.nexus.rinde.iam.infrastructure.persistence.jpa.converters;

import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/** Guarda el TenantId como UUID; se aplica solo a todo atributo de ese tipo. */
@Converter(autoApply = true)
public class TenantIdConverter implements AttributeConverter<TenantId, UUID> {

  @Override
  public UUID convertToDatabaseColumn(TenantId attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public TenantId convertToEntityAttribute(UUID dbData) {
    return dbData == null ? null : new TenantId(dbData);
  }
}
