package AgroProtect.useradapter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MINIMAL USER REPOSITORY - Extracted from identity module
 *
 * When integrating: DELETE and use tn.esprit.agroprotect.identity.repository.UserRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Core methods needed by microcredit
    Optional<User> findById(Long id);

    Optional<User> findByUuid(String uuid);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Active user lookup (important for validation)
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.uuid = :uuid AND u.deletedAt IS NULL")
    Optional<User> findActiveByUuid(@Param("uuid") String uuid);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveByIdWithRoles(@Param("id") Long id);
}