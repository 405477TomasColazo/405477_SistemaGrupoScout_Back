package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentFilters {
    private Integer memberId;
    private Integer familyGroupId;
    private Integer sectionId;
    private String dateFrom;
    private String dateTo;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private PaymentStatus status;
    private String paymentMethod;
    private String memberName;
}
