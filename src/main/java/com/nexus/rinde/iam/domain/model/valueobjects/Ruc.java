package com.nexus.rinde.iam.domain.model.valueobjects;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import java.util.regex.Pattern;

/** RUC de la empresa: 11 dígitos numéricos. La unicidad entre empresas la valida el servicio. */
public record Ruc(String number) {

  private static final Pattern ELEVEN_DIGITS = Pattern.compile("\\d{11}");

  public Ruc {
    if (number == null || !ELEVEN_DIGITS.matcher(number).matches()) {
      throw new BusinessRuleException("El RUC debe tener 11 dígitos numéricos.");
    }
  }
}
