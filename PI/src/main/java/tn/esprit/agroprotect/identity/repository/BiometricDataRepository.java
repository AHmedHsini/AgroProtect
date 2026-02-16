package tn.esprit.agroprotect.identity.repository;

import tn.esprit.agroprotect.identity.entity.BiometricData;
import tn.esprit.agroprotect.identity.entity.BiometricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for biometric data.
 */
@Repository
public interface BiometricDataRepository extends JpaRepository<BiometricData, Long> {

    List<BiometricData> findByUserId(Long userId);

    @Query("SELECT b FROM BiometricData b WHERE b.user.id = :userId AND b.biometricType = :type AND b.isActive = true")
    Optional<BiometricData> findActiveByUserIdAndType(@Param("userId") Long userId,
            @Param("type") BiometricType type);

    @Query("SELECT b FROM BiometricData b WHERE b.user.id = :userId AND b.isActive = true")
    List<BiometricData> findActiveByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndBiometricTypeAndIsActiveTrue(Long userId, BiometricType type);
}
