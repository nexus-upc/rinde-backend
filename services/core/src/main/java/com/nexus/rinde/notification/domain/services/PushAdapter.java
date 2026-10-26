package com.nexus.rinde.notification.domain.services;

import com.nexus.rinde.notification.domain.model.valueobjects.TripAssignmentNotice;

/** Puerto para enviar el aviso al dispositivo asociado al conductor. */
public interface PushAdapter {

  void send(TripAssignmentNotice notice);
}
