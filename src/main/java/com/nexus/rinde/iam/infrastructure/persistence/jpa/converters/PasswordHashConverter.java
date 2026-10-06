package com.nexus.rinde.iam.infrastructure.persistence.jpa.converters;

import com.nexus.rinde.iam.domain.model.valueobjects.PasswordHash;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Guarda el PasswordHash como texto; es nulo mientras el usuario invitado no define su contraseña.
 */
@Converter(autoApply = true)
public class PasswordHashConverter implements AttributeConverter<PasswordHash, String> {

  @Override
  public String convertToDatabaseColumn(PasswordHash attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public PasswordHash convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new PasswordHash(dbData);
  }
}
