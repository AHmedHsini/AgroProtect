package com.agriplatform.identity.repository;

import com.agriplatform.identity.entity.AuditLog;
import com.agriplatform.identity.entity.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(a.userId = :userId OR :userId IS NULL) AND " +
            "(a.action = :action OR :action IS NULL) AND " +
            "(a.status = :status OR :status IS NULL) AND " +
            "(a.createdAt >= :startDate OR :startDate IS NULL) AND " +
            "(a.createdAt <= :endDate OR :endDate IS NULL)")
    Page<AuditLog> searchAuditLogs(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("status") AuditStatus status,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.action = :action AND a.status = 'FAILURE' AND a.createdAt > :since")
    long countRecentFailures(@Param("userId") Long userId, @Param("action") String action,
            @Param("since") Instant since);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.action = :action AND a.createdAt > :since")
    long countRecentActionsByIp(@Param("ipAddress") String ipAddress, @Param("action") String action,
            @Param("since") Instant since);
}
