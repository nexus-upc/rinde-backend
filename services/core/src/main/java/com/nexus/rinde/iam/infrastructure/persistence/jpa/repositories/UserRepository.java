package com.nexus.rinde.iam.infrastructure.persistence.jpa.repositories;

import com.nexus.rinde.iam.domain.model.aggregates.User;
import com.nexus.rinde.iam.domain.model.valueobjects.Email;
import com.nexus.rinde.iam.domain.model.valueobjects.TenantId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio de usuarios: por correo, por empresa y por el hash de un enlace de acceso. */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(Email email);

  boolean existsByEmail(Email email);

  List<User> findByTenantId(TenantId tenantId);

  /** Los enlaces llegan sin correo ni empresa, por eso se buscan por su hash (único). */
  @Query("select u from User u join u.accessTokens t where t.value = :tokenHash")
  Optional<User> findByAccessTokenHash(@Param("tokenHash") String tokenHash);
}
