package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePaymentStatusRequest {
    private PaymentStatus status;
    private String reason;
}