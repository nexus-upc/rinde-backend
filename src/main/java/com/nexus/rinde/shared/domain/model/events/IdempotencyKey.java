package com.nexus.rinde.shared.domain.model.events;

/** Clave que permite a un consumidor descartar un evento que ya procesó (CRN-08). */
public interface IdempotencyKey {

  String idempotencyKey();
}
