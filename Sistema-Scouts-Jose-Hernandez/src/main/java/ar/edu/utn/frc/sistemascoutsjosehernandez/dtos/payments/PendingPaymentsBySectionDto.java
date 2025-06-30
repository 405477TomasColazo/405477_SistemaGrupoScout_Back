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
public class PendingPaymentsBySectionDto {
    private Integer sectionId;
    private String sectionName;
    private long totalPendingFees;
    private BigDecimal totalPendingAmount;
    private long membersWithPendingPayments;
}