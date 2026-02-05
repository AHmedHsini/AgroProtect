package com.agriplatform.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Device session response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSessionResponse {

    private Long id;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String osName;
    private String browserName;
    private String ipAddress;
    private String locationCountry;
    private String locationCity;
    private Boolean isCurrent;
    private Boolean isTrusted;
    private Instant lastActiveAt;
    private Instant createdAt;
}
