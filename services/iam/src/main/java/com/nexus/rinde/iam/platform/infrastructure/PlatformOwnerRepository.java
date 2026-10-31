package com.nexus.rinde.iam.platform.infrastructure;

import com.nexus.rinde.iam.platform.domain.PlatformOwner;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformOwnerRepository extends JpaRepository<PlatformOwner, UUID> {
  Optional<PlatformOwner> findByEmailIgnoreCase(String email);
}
