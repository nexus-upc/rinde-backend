package com.nexus.rinde.iam.domain.services;

import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.queries.GetUserByIdQuery;
import com.nexus.rinde.iam.domain.model.queries.GetUsersByTenantQuery;
import java.util.List;
import java.util.Optional;

/** Consultas de usuarios, siempre acotadas a una empresa. */
public interface UserQueryService {

  List<User> handle(GetUsersByTenantQuery query);

  Optional<User> handle(GetUserByIdQuery query);
}
