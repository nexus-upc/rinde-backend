package com.nexus.rinde.iam.infrastructure.persistence.jpa.converters;

import com.nexus.rinde.iam.domain.model.valueobjects.Ruc;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Guarda el Ruc como texto de 11 dígitos. */
@Converter(autoApply = true)
public class RucConverter implements AttributeConverter<Ruc, String> {

  @Override
  public String convertToDatabaseColumn(Ruc attribute) {
    return attribute == null ? null : attribute.number();
  }

  @Override
  public Ruc convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new Ruc(dbData);
  }
}
