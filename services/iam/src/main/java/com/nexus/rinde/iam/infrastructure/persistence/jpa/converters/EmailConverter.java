package com.nexus.rinde.iam.infrastructure.persistence.jpa.converters;

import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Guarda el Email como texto en minúsculas. */
@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

  @Override
  public String convertToDatabaseColumn(Email attribute) {
    return attribute == null ? null : attribute.address();
  }

  @Override
  public Email convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new Email(dbData);
  }
}
