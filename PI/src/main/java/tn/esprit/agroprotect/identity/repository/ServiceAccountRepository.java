package tn.esprit.agroprotect.identity.repository;

import tn.esprit.agroprotect.identity.entity.ServiceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, Long> {

    Optional<ServiceAccount> findByServiceName(String serviceName);

    Optional<ServiceAccount> findByApiKeyHash(String apiKeyHash);

    boolean existsByServiceName(String serviceName);
}
