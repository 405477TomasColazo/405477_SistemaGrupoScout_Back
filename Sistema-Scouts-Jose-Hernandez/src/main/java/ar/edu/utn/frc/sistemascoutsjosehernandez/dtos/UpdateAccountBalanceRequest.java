package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateAccountBalanceRequest {
    
    @NotNull(message = "New balance is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Balance must be greater than zero")
    private BigDecimal newBalance;
    
    @NotNull(message = "Reason is required")
    @Size(min = 5, max = 255, message = "Reason must be between 5 and 255 characters")
    private String reason;
}