package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentStatisticsDto {
    private long totalPayments;
    private long completedPayments;
    private long pendingPayments;
    private long failedPayments;
    private BigDecimal totalAmount;
    private BigDecimal completedAmount;
    private BigDecimal pendingAmount;
    private BigDecimal averagePaymentAmount;
}