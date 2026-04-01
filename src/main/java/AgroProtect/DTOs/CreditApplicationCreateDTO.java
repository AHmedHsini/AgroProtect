// CreditApplicationCreateDTO.java
package AgroProtect.DTOs;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreditApplicationCreateDTO {

    @Positive(message = "Amount must be positive")
    @Max(value = 50000, message = "Maximum amount is 50000 TND")
    private double requestedAmount;

    @Min(value = 1, message = "Minimum duration is 1 month")
    @Max(value = 60, message = "Maximum duration is 60 months")
    private int requestedDurationMonths;

    @NotBlank(message = "Purpose is required")
    @Size(max = 500, message = "Purpose too long")
    private String purpose;

    @NotNull(message = "User ID is required")
    private Long userId;  // <-- ADD THIS
}