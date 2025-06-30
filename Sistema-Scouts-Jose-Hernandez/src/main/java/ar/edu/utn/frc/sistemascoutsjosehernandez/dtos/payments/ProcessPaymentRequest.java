package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessPaymentRequest {
    private Map<String, Object> paymentFormData; // Generic data for any payment method
    private String preferenceId;
    private List<Integer> feeIds;
    private String paymentMethod; // Optional: card, bank_transfer, ticket, etc.
}
