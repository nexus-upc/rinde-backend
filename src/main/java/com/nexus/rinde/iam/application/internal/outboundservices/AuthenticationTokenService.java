package com.nexus.rinde.iam.application.internal.outboundservices;

import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.valueobjects.AuthenticationResult;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantStatus;

/** Puerto para emitir el token de acceso; lo implementa la capa de infraestructura con JWT. */
public interface AuthenticationTokenService {

  AuthenticationResult issueFor(User user, TenantStatus tenantStatus);
}
