package com.nexus.rinde.expense.infrastructure.remote;

import com.nexus.rinde.configuration.remote.RemoteServiceUnavailableException;
import com.nexus.rinde.expense.interfaces.acl.ExpenseContextFacade;
import com.nexus.rinde.expense.interfaces.acl.ExpenseSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Consulta los gastos en el servicio Expense separado; reemplaza la copia antigua de core. */
@Service
@Primary
@ConditionalOnProperty(name = "rinde.expense.remote-enabled", havingValue = "true")
public class HttpExpenseContextFacade implements ExpenseContextFacade {

  private static final ParameterizedTypeReference<List<ExpenseSummary>> LIST =
      new ParameterizedTypeReference<>() {};

  private final RestClient client;

  public HttpExpenseContextFacade(@Qualifier("expenseRestClient") RestClient client) {
    this.client = client;
  }

  @Override
  public List<ExpenseSummary> findApprovedExpensesByTripId(UUID tenantId, UUID tripId) {
    return list(
        "/api/internal/v1/expenses/tenants/{tenant}/trips/{trip}/approved", tenantId, tripId);
  }

  @Override
  public List<ExpenseSummary> findByTenantIdAndTripId(UUID tenantId, UUID tripId) {
    return list("/api/internal/v1/expenses/tenants/{tenant}/trips/{trip}", tenantId, tripId);
  }

  @Override
  public List<ExpenseSummary> findByTenantId(UUID tenantId) {
    return list("/api/internal/v1/expenses/tenants/{tenant}", tenantId);
  }

  private List<ExpenseSummary> list(String path, Object... variables) {
    try {
      List<ExpenseSummary> body = client.get().uri(path, variables).retrieve().body(LIST);
      return body == null ? List.of() : body;
    } catch (RestClientException ex) {
      throw new RemoteServiceUnavailableException("gastos", ex);
    }
  }
}
