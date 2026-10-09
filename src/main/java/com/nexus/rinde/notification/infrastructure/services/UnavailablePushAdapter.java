package com.nexus.rinde.notification.infrastructure.services;

import com.nexus.rinde.notification.domain.model.valueobjects.TripAssignmentNotice;
import com.nexus.rinde.notification.domain.services.PushAdapter;

/** Adaptador explícito para el entorno local mientras no exista integración push. */
public class UnavailablePushAdapter implements PushAdapter {

  @Override
  public void send(TripAssignmentNotice notice) {
    throw new PushAdapterUnavailableException(
        "No hay token de dispositivo ni proveedor push configurado.");
  }
}
