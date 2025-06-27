package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialStatsDto {
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal pendingPayments;
    private long totalPaymentCount;
    private long pendingPaymentCount;
    private long completedPaymentCount;
}