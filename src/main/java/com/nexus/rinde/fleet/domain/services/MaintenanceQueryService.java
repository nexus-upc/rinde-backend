package com.nexus.rinde.fleet.domain.services;

import com.nexus.rinde.fleet.domain.model.queries.ListMaintenanceAlertsQuery;
import com.nexus.rinde.fleet.domain.model.valueobjects.MaintenanceAlert;
import java.util.List;

/** Servicio de aplicación para consultas sobre mantenimientos y alertas. */
public interface MaintenanceQueryService {

  List<MaintenanceAlert> handle(ListMaintenanceAlertsQuery query);
}
