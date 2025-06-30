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
public class PaymentDto {
    private Integer id;
    private Integer memberId;
    private String memberName;
    private String memberLastName;
    private BigDecimal amount;
    private String paymentDate;
    private String status; // completed, processing, failed
    private String referenceId;
    private String paymentMethod;
    private List<PaymentItemDTO> items;
}
