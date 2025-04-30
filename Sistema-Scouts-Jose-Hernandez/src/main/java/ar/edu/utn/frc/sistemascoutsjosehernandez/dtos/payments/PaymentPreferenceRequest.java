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
public class PaymentPreferenceRequest {
    private Integer memberId;
    private String paymentType;
    private BigDecimal totalAmount;
    private String externalReference;
    private List<PaymentItemDTO> items;
}
