package com.nexus.rinde.fleet.domain.model.valueobjects;

/** Estado de mantenimiento calculado según la proximidad de su fecha límite. */
public enum MaintenanceState {
  UP_TO_DATE,
  DUE_SOON,
  OVERDUE
}
