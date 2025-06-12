package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final PaymentService paymentService;

    @GetMapping("/fees/{memberId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FeeDto>> getPendingFees(@PathVariable Integer memberId) {
        return ResponseEntity.ok(paymentService.getPendingFees(memberId));
    }

    @PostMapping("/create-preference")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentPreferenceResponse> createPaymentPreference(
            @RequestBody PaymentPreferenceRequest request) {
        PaymentPreferenceResponse response = paymentService.createPaymentPreference(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProcessPaymentResponse> processPayment(
            @RequestBody ProcessPaymentRequest request) {
        ProcessPaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentsHistoryResponse> getPaymentHistory(
            @RequestParam(required = false) Integer memberId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PaymentFilters filters = new PaymentFilters();
        filters.setMemberId(memberId);
        filters.setDateFrom(dateFrom);
        filters.setDateTo(dateTo);
        filters.setMinAmount(minAmount);

        PaymentsHistoryResponse response = paymentService.getPaymentsHistory(filters, page, limit);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload,
                                               @RequestHeader Map<String, String> headers) {
        try {
            System.out.println("Received MercadoPago webhook: " + payload);
            
            // Validate webhook signature for security (optional but recommended)
            String signature = headers.get("x-signature");
            String webhookSecret = "your-webhook-secret"; // TODO: Move to config
            
            if (!paymentService.validateWebhookSignature(payload.toString(), signature, webhookSecret)) {
                System.out.println("Invalid webhook signature - rejecting request");
                return ResponseEntity.status(401).body("Invalid signature");
            }
            
            String type = (String) payload.get("type");
            String action = (String) payload.get("action");
            
            // Handle payment notifications
            if ("payment".equals(type)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                if (data != null && data.containsKey("id")) {
                    String paymentId = data.get("id").toString();
                    
                    // Only process relevant actions to avoid unnecessary API calls
                    if ("payment.updated".equals(action) || "payment.created".equals(action)) {
                        paymentService.updatePaymentStatusFromWebhook(paymentId);
                        System.out.println("Processed webhook for payment: " + paymentId);
                    }
                }
            }
            
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            e.printStackTrace();
            // Always return OK to avoid webhook retries from MercadoPago
            return ResponseEntity.ok("OK");
        }
    }

}
