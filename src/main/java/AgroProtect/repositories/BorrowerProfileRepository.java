package AgroProtect.repositories;

import AgroProtect.entities.BorrowerProfile;
import AgroProtect.useradapter.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowerProfileRepository extends JpaRepository<BorrowerProfile, Long> {
    Optional<BorrowerProfile> findByUser(User user); // This method must exist
}