package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplyBalanceResponse {
    private String status; // "success" or "error"
    private String message;
    private BigDecimal balanceUsed;
    private BigDecimal remainingBalance;
    private List<FeeDto> updatedFees;
    private Integer feesPaidCompletely; // Number of fees that were paid completely with balance
}