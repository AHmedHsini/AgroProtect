package tn.esprit.agroprotect.identity.repository;

import tn.esprit.agroprotect.identity.entity.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {

    List<DeviceSession> findByUserIdAndRevokedFalse(Long userId);

    Optional<DeviceSession> findByUserIdAndDeviceId(Long userId, String deviceId);

    @Query("SELECT ds FROM DeviceSession ds WHERE ds.user.id = :userId AND ds.revoked = false ORDER BY ds.lastActiveAt DESC")
    List<DeviceSession> findActiveSessionsByUser(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE DeviceSession ds SET ds.revoked = true, ds.revokedAt = :now WHERE ds.user.id = :userId AND ds.id != :currentSessionId")
    void revokeOtherSessions(@Param("userId") Long userId, @Param("currentSessionId") Long currentSessionId,
            @Param("now") Instant now);

    @Modifying
    @Query("UPDATE DeviceSession ds SET ds.revoked = true, ds.revokedAt = :now WHERE ds.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE DeviceSession ds SET ds.lastActiveAt = :now WHERE ds.id = :sessionId")
    void updateLastActive(@Param("sessionId") Long sessionId, @Param("now") Instant now);

    long countByUserIdAndRevokedFalse(Long userId);
}
