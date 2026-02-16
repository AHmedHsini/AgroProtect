package tn.esprit.agroprotect.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Device session entity for tracking user sessions across devices.
 * 
 * Enables:
 * - Multi-device session management
 * - Session revocation per device
 * - Suspicious activity detection via location/IP changes
 */
@Entity
@Table(name = "device_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    @Builder.Default
    private DeviceType deviceType = DeviceType.UNKNOWN;

    @Column(name = "os_name", length = 100)
    private String osName;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "browser_name", length = 100)
    private String browserName;

    @Column(name = "browser_version", length = 50)
    private String browserVersion;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "location_country", length = 100)
    private String locationCountry;

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @Column(name = "is_current")
    @Builder.Default
    private Boolean isCurrent = false;

    @Column(name = "is_trusted")
    @Builder.Default
    private Boolean isTrusted = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public void updateActivity() {
        this.lastActiveAt = Instant.now();
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
        this.isCurrent = false;
    }

    public boolean isActive() {
        return !revoked;
    }
}
