package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.FamilyGroup;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Payment;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FamilyGroupRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.PaymentRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.reports.service.PaymentReceiptService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.PaymentService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final PaymentReceiptService paymentReceiptService;
    private final PaymentRepository paymentRepository;
    private final FamilyGroupRepository familyGroupRepository;
    private final SecurityUtils securityUtils;

    @GetMapping("/fees/{memberId}")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<List<FeeDto>> getPendingFees(@PathVariable Integer memberId) {
        return ResponseEntity.ok(paymentService.getPendingFees(memberId));
    }

    @PostMapping("/create-preference")
    @PreAuthorize("hasAnyRole('FAMILY', 'ADMIN')")
    public ResponseEntity<PaymentPreferenceResponse> createPaymentPreference(
            @RequestBody PaymentPreferenceRequest request) {
        PaymentPreferenceResponse response = paymentService.createPaymentPreference(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('FAMILY', 'ADMIN')")
    public ResponseEntity<ProcessPaymentResponse> processPayment(
            @RequestBody ProcessPaymentRequest request) {
        ProcessPaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
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

    @GetMapping("/balance/{memberId}")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getMemberBalance(@PathVariable Integer memberId) {
        BigDecimal balance = paymentService.getMemberBalance(memberId);
        Map<String, BigDecimal> response = Map.of("balance", balance);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply-balance")
    @PreAuthorize("hasAnyRole('FAMILY', 'ADMIN')")
    public ResponseEntity<ApplyBalanceResponse> applyBalanceToFees(
            @RequestBody ApplyBalanceRequest request) {
        ApplyBalanceResponse response = paymentService.applyBalanceToFees(request);
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

    @GetMapping("/{paymentId}/receipt")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<byte[]> downloadPaymentReceipt(@PathVariable Integer paymentId) {
        try {
            // Validate access permissions
            if (!canAccessPaymentReceipt(paymentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if receipt can be generated
            if (!paymentReceiptService.canGenerateReceipt(paymentId)) {
                return ResponseEntity.notFound().build();
            }

            // Generate receipt using JasperReports
            byte[] receipt = paymentReceiptService.generatePaymentReceipt(paymentId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "comprobante-pago-" + paymentId + ".pdf");
            
            return new ResponseEntity<>(receipt, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            // Fallback to old method if JasperReports fails
            byte[] receipt = paymentService.generatePaymentReceipt(paymentId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "comprobante-pago-" + paymentId + ".pdf");
            
            return new ResponseEntity<>(receipt, headers, HttpStatus.OK);
        }
    }

    @GetMapping("/{paymentId}/receipt/preview")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<byte[]> previewPaymentReceipt(@PathVariable Integer paymentId) {
        try {
            // Validate access permissions
            if (!canAccessPaymentReceipt(paymentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if receipt can be generated
            if (!paymentReceiptService.canGenerateReceipt(paymentId)) {
                return ResponseEntity.notFound().build();
            }

            // Generate receipt using JasperReports
            byte[] receipt = paymentReceiptService.generatePaymentReceipt(paymentId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "comprobante-pago-" + paymentId + ".pdf");
            
            return new ResponseEntity<>(receipt, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean canAccessPaymentReceipt(Integer paymentId) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            
            // Admins and educators can access all receipts
            if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("EDUCATOR")) {
                return true;
            }

            // Families can only access receipts for their own family members
            if (securityUtils.hasRole("FAMILY")) {
                Payment payment = paymentRepository.findById(paymentId).orElse(null);
                if (payment == null) {
                    return false;
                }

                // Find the user's family group and check if the payment belongs to any member
                FamilyGroup userFamilyGroup = familyGroupRepository.findFamilyGroupsByUser_Id(currentUser.getId());
                if (userFamilyGroup == null) {
                    return false;
                }
                
                return userFamilyGroup.getMembers().stream()
                       .anyMatch(member -> member.getId().equals(payment.getMemberId()));
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

}
