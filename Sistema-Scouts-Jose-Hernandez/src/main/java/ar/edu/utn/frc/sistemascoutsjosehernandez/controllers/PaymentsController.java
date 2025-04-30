package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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

}
