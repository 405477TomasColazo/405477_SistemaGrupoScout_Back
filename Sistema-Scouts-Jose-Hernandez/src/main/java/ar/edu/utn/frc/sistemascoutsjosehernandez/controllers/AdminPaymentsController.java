package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentsController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<PaymentsHistoryResponse> getAllPayments(
            @RequestParam(required = false) Integer memberId,
            @RequestParam(required = false) Integer familyGroupId,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String memberName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {

        PaymentFilters filters = PaymentFilters.builder()
                .memberId(memberId)
                .familyGroupId(familyGroupId)
                .sectionId(sectionId)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .status(status)
                .paymentMethod(paymentMethod)
                .memberName(memberName)
                .build();

        PaymentsHistoryResponse response = paymentService.getAllPaymentsForAdmin(filters, page, limit);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentDto> updatePaymentStatus(
            @PathVariable Integer paymentId,
            @RequestBody UpdatePaymentStatusRequest request) {
        
        PaymentDto updatedPayment = paymentService.updatePaymentStatus(paymentId, request.getStatus(), request.getReason());
        return ResponseEntity.ok(updatedPayment);
    }

    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<byte[]> downloadPaymentReceipt(@PathVariable Integer paymentId) {
        byte[] receipt = paymentService.generatePaymentReceipt(paymentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "comprobante-pago-" + paymentId + ".pdf");
        
        return new ResponseEntity<>(receipt, headers, HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<PaymentStatisticsDto> getPaymentStatistics(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer sectionId) {
        
        PaymentStatisticsDto statistics = paymentService.getPaymentStatistics(dateFrom, dateTo, sectionId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/pending-by-section")
    public ResponseEntity<List<PendingPaymentsBySectionDto>> getPendingPaymentsBySection() {
        List<PendingPaymentsBySectionDto> pendingPayments = paymentService.getPendingPaymentsBySection();
        return ResponseEntity.ok(pendingPayments);
    }
}