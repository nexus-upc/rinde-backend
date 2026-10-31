package com.nexus.rinde.iam.domain.model.valueobjects;

import com.nexus.rinde.shared.domain.exceptions.BusinessRuleException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Correo electrónico válido; se guarda en minúsculas para que la búsqueda no distinga mayúsculas.
 */
public record Email(String address) {

  private static final Pattern FORMAT =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
  private static final int MAX_LENGTH = 150;

  public Email {
    if (address == null) {
      throw new BusinessRuleException("El correo electrónico es obligatorio.");
    }
    address = address.trim().toLowerCase(Locale.ROOT);
    if (address.length() > MAX_LENGTH || !FORMAT.matcher(address).matches()) {
      throw new BusinessRuleException("El correo electrónico no tiene un formato válido.");
    }
  }
}
