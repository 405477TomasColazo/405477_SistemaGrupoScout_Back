package ar.edu.utn.frc.sistemascoutsjosehernandez.reports.controller;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.FamilyGroup;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Payment;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FamilyGroupRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.PaymentRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.reports.service.PaymentReceiptService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Slf4j
@Tag(name = "Reports", description = "Report generation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final PaymentReceiptService paymentReceiptService;
    private final PaymentRepository paymentRepository;
    private final FamilyGroupRepository familyGroupRepository;
    private final SecurityUtils securityUtils;

    public ReportController(PaymentReceiptService paymentReceiptService,
                          PaymentRepository paymentRepository,
                          FamilyGroupRepository familyGroupRepository,
                          SecurityUtils securityUtils) {
        this.paymentReceiptService = paymentReceiptService;
        this.paymentRepository = paymentRepository;
        this.familyGroupRepository = familyGroupRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/payment-receipt/{paymentId}")
    @Operation(summary = "Generate payment receipt", 
               description = "Generates a PDF receipt for a specific payment. Families can only access their own payment receipts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Receipt generated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - not authorized to view this receipt"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Error generating receipt")
    })
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<byte[]> generatePaymentReceipt(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable Integer paymentId) {
        
        try {
            log.info("Request to generate payment receipt for payment ID: {}", paymentId);

            // Validate access permissions
            if (!canAccessPaymentReceipt(paymentId)) {
                log.warn("Access denied for payment receipt. Payment ID: {}, User: {}", 
                        paymentId, securityUtils.getCurrentUser().getUsername());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if receipt can be generated
            if (!paymentReceiptService.canGenerateReceipt(paymentId)) {
                log.error("Cannot generate receipt for payment ID: {}", paymentId);
                return ResponseEntity.notFound().build();
            }

            // Generate the receipt
            byte[] receiptPdf = paymentReceiptService.generatePaymentReceipt(paymentId);

            // Prepare HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("comprobante-pago-%d.pdf", paymentId));
            headers.setContentLength(receiptPdf.length);

            log.info("Payment receipt generated successfully for payment ID: {}", paymentId);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(receiptPdf);

        } catch (Exception e) {
            log.error("Error generating payment receipt for payment ID: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/payment-receipt/{paymentId}/preview")
    @Operation(summary = "Preview payment receipt", 
               description = "Generates a PDF receipt for preview (inline display)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Receipt preview generated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Error generating preview")
    })
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<byte[]> previewPaymentReceipt(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable Integer paymentId) {
        
        try {
            log.info("Request to preview payment receipt for payment ID: {}", paymentId);

            // Validate access permissions
            if (!canAccessPaymentReceipt(paymentId)) {
                log.warn("Access denied for payment receipt preview. Payment ID: {}, User: {}", 
                        paymentId, securityUtils.getCurrentUser().getUsername());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if receipt can be generated
            if (!paymentReceiptService.canGenerateReceipt(paymentId)) {
                log.error("Cannot generate preview for payment ID: {}", paymentId);
                return ResponseEntity.notFound().build();
            }

            // Generate the receipt
            byte[] receiptPdf = paymentReceiptService.generatePaymentReceipt(paymentId);

            // Prepare HTTP headers for inline display
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", 
                String.format("comprobante-pago-%d.pdf", paymentId));

            log.info("Payment receipt preview generated successfully for payment ID: {}", paymentId);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(receiptPdf);

        } catch (Exception e) {
            log.error("Error generating payment receipt preview for payment ID: {}", paymentId, e);
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
            log.error("Error checking access permissions for payment receipt: {}", paymentId, e);
            return false;
        }
    }
}