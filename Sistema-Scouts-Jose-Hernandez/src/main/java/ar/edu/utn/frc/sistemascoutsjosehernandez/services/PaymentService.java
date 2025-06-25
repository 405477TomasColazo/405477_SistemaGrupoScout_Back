package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Payment;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentItem;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventRegistration;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.EventRegistrationRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FeeRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.PaymentRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.UserRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FamilyGroupRepository;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// iText PDF imports
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.io.font.constants.StandardFonts;


@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${token.mp}")
    private String mercadoPagoAccessToken;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final FeeRepository feeRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final MemberRepository memberRepository;
    private final FamilyGroupRepository familyGroupRepository;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
    }



    public List<FeeDto> getPendingFees(Integer memberId){
        List<Fee> feeList = feeRepository.findByMemberIdAndStatus(memberId, PaymentStatus.PENDING);
        List<FeeDto> feeDtos = new ArrayList<>();
        feeList.forEach(f -> {
            FeeDto feeDto = toFeeDto(f);
            feeDtos.add(feeDto);
        });
        return feeDtos;
    }

    private FeeDto toFeeDto(Fee fee){
        return FeeDto.builder()
                .id(fee.getId())
                .description(fee.getDescription())
                .amount(fee.getAmount())
                .period(fee.getPeriod())
                .memberId(fee.getMember().getId())
                .status(fee.getStatus().name().toLowerCase())
                .build();
    }

    @Transactional
    public PaymentPreferenceResponse createPaymentPreference(PaymentPreferenceRequest request)  {
        try {
            PreferenceClient client = new PreferenceClient();

            List<PreferenceItemRequest> items = request.getItems().stream()
                    .map(item -> PreferenceItemRequest.builder()
                            .title(item.getDescription() + " - " + item.getPeriod())
                            .quantity(1)
                            .unitPrice(item.getAmount())
                            .build()).toList();

            //Agregar BackUrls cuando los tenga
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(request.getExternalReference())
                    .build();

            // Generar UUID v4 para X-Idempotency-Key
        String idempotencyKey = UUID.randomUUID().toString();

        // Configurar el encabezado en el cliente
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Idempotency-Key", idempotencyKey);

        Preference preference = client.create(preferenceRequest, MPRequestOptions.builder()
                .customHeaders(headers)
                .build());

            PaymentPreferenceResponse response = new PaymentPreferenceResponse();
            response.setPreferenceId(preference.getId());
            response.setInitPoint(preference.getInitPoint());

            return response;
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request)  {
        try {
            PaymentClient client = new PaymentClient();
            
            // Detect payment method type from the form data
            String paymentMethodType = detectPaymentMethodType(request.getPaymentFormData());
            
            // Build payment request based on payment method type
            PaymentCreateRequest paymentCreateRequest = buildPaymentRequest(request, paymentMethodType);

            // Generar UUID v4 para X-Idempotency-Key
            String idempotencyKey = UUID.randomUUID().toString();

            // Configurar el encabezado en el cliente
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Idempotency-Key", idempotencyKey);

            com.mercadopago.resources.payment.Payment mpPayment = client.create(paymentCreateRequest,MPRequestOptions.builder()
                    .customHeaders(headers)
                    .build());

            Payment payment = Payment.builder()
                    .memberId(getMemberIdFromFees(request.getFeeIds()))
                    .amount(mpPayment.getTransactionAmount())
                    .paymentDate(LocalDate.now().toString())
                    .status(mapMercadoPagoStatus(mpPayment.getStatus()))
                    .referenceId(mpPayment.getId().toString())
                    .paymentMethod(mpPayment.getPaymentMethodId())
                    .build();

            List<PaymentItem> items = createPaymentItemsFromFees(request.getFeeIds());
            // Set bidirectional relationship
            items.forEach(item -> item.setPayment(payment));
            payment.setItems(items);

            Payment savedPayment = paymentRepository.save(payment);

                for (Integer id : request.getFeeIds()){
                    Fee fee = feeRepository.findById(id).orElse(null);
                    if (fee != null){
                        fee.setStatus(mapMercadoPagoStatus(mpPayment.getStatus()));
                        feeRepository.save(fee);
                        
                        // Update EventRegistration payment status if this fee is for an event
                        updateEventRegistrationPaymentStatus(fee, mapMercadoPagoStatus(mpPayment.getStatus()), savedPayment.getId().intValue());
                    }

                }

            ProcessPaymentResponse response = new ProcessPaymentResponse();
            if (mpPayment.getStatus().equalsIgnoreCase("approved")) {
                response.setStatus("success");
            }else if (mpPayment.getStatus().equalsIgnoreCase("pending")
                    || mpPayment.getStatus().equalsIgnoreCase("in_process")
                    || mpPayment.getStatus().equalsIgnoreCase("authorized")) {
                response.setStatus("processing");
            }else {
                response.setStatus("failed");
            }

            response.setPayment(mapToPaymentDTO(savedPayment));

            return response;
        }catch (MPException | MPApiException e) {
            ProcessPaymentResponse response = new ProcessPaymentResponse();
            response.setStatus("error");
            response.setMessage("Error al procesar el pago: " + e.getMessage());
            return response;
        }
    }

    public PaymentsHistoryResponse getPaymentsHistory(PaymentFilters filters, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        
        // Get current user and check if they are admin
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isAdmin = currentUser.getRolesXUser().stream()
                .anyMatch(roleXUser -> roleXUser.getRole().getDescription().equals("ROLE_ADMIN"));
        
        Page<Payment> paymentsPage;
        
        if (isAdmin) {
            // Admin can see all payments
            paymentsPage = paymentRepository.findByFilters(
                    filters.getMemberId(),
                    filters.getDateFrom(),
                    filters.getDateTo(),
                    filters.getMinAmount(),
                    pageRequest
            );
        } else {
            // Non-admin users can only see payments from their family
            FamilyGroup familyGroup = familyGroupRepository.findFamilyGroupsByUser_Id(currentUser.getId());
            if (familyGroup == null) {
                throw new RuntimeException("Family group not found");
            }
            
            List<Integer> familyMemberIds = memberRepository.findAllByFamilyGroup_Id(familyGroup.getId())
                    .stream()
                    .map(Member::getId)
                    .collect(Collectors.toList());
            
            // Filter payments to only include family members
            paymentsPage = paymentRepository.findByFilters(
                    filters.getMemberId(),
                    filters.getDateFrom(),
                    filters.getDateTo(),
                    filters.getMinAmount(),
                    pageRequest
            );
            
            // Additional filtering by family members
            paymentsPage = paymentsPage.map(payment -> {
                if (familyMemberIds.contains(payment.getMemberId())) {
                    return payment;
                }
                return null;
            });
            
            // Remove null entries
            List<Payment> filteredPayments = paymentsPage.getContent().stream()
                    .filter(payment -> payment != null && familyMemberIds.contains(payment.getMemberId()))
                    .collect(Collectors.toList());
            
            // Create new page with filtered results
            paymentsPage = new org.springframework.data.domain.PageImpl<>(
                    filteredPayments,
                    pageRequest,
                    filteredPayments.size()
            );
        }

        List<PaymentDto> paymentDTOs = paymentsPage.getContent().stream()
                .map(this::mapToPaymentDTO)
                .collect(Collectors.toList());

        PaymentsHistoryResponse response = new PaymentsHistoryResponse();
        response.setPayments(paymentDTOs);
        response.setTotal((int) paymentsPage.getTotalElements());

        return response;
    }

    public PaymentsHistoryResponse getAllPaymentsForAdmin(PaymentFilters filters, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit);

        Page<Payment> paymentsPage = paymentRepository.findByAdminFilters(
                filters.getMemberId(),
                filters.getFamilyGroupId(),
                filters.getSectionId(),
                filters.getDateFrom(),
                filters.getDateTo(),
                filters.getMinAmount(),
                filters.getMaxAmount(),
                filters.getStatus(),
                filters.getPaymentMethod(),
                filters.getMemberName(),
                pageRequest
        );

        List<PaymentDto> paymentDTOs = paymentsPage.getContent().stream()
                .map(this::mapToPaymentDTO)
                .collect(Collectors.toList());

        PaymentsHistoryResponse response = new PaymentsHistoryResponse();
        response.setPayments(paymentDTOs);
        response.setTotal((int) paymentsPage.getTotalElements());

        return response;
    }

    @Transactional
    public PaymentDto updatePaymentStatus(Integer paymentId, PaymentStatus newStatus, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(newStatus);
        
        Payment savedPayment = paymentRepository.save(payment);

        for (PaymentItem item : payment.getItems()) {
            if (item.getFee() != null) {
                Fee fee = item.getFee();
                fee.setStatus(newStatus);
                feeRepository.save(fee);
                
                updateEventRegistrationPaymentStatus(fee, newStatus, paymentId);
            }
        }

        return mapToPaymentDTO(savedPayment);
    }

    public byte[] generatePaymentReceipt(Integer paymentId) {
        Payment payment = paymentRepository.findByIdWithItems(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        return generatePDFReceipt(payment);
    }

    private byte[] generatePDFReceipt(Payment payment) {
        try {
            // Get member information
            Member member = memberRepository.findById(payment.getMemberId()).orElse(null);
            String memberName = member != null ? member.getName() + " " + member.getLastname() : "Miembro no encontrado";
            
            // Create ByteArrayOutputStream to hold PDF bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Initialize PDF writer and document
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Set up fonts
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            // Header
            document.add(new Paragraph("GRUPO SCOUT JOSÉ HERNÁNDEZ")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));
            
            document.add(new Paragraph("COMPROBANTE DE PAGO")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Payment details table
            Table paymentTable = new Table(2);
            paymentTable.setWidth(UnitValue.createPercentValue(100));
            
            // Add payment details
            addTableRow(paymentTable, "Referencia:", payment.getReferenceId() != null ? payment.getReferenceId() : "N/A", boldFont, regularFont);
            addTableRow(paymentTable, "Fecha:", payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "N/A", boldFont, regularFont);
            addTableRow(paymentTable, "Protagonista:", memberName, boldFont, regularFont);
            addTableRow(paymentTable, "Monto Total:", "$" + payment.getAmount(), boldFont, regularFont);
            addTableRow(paymentTable, "Estado:", payment.getStatus().toString(), boldFont, regularFont);
            addTableRow(paymentTable, "Método:", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A", boldFont, regularFont);
            
            document.add(paymentTable);
            document.add(new Paragraph("\n"));
            
            // Payment items section
            document.add(new Paragraph("CONCEPTOS PAGADOS")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setMarginBottom(10));
            
            // Items table
            Table itemsTable = new Table(3);
            itemsTable.setWidth(UnitValue.createPercentValue(100));
            
            // Headers
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Descripción").setFont(boldFont)));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Período").setFont(boldFont)));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Monto").setFont(boldFont)));
            
            // Items
            for (PaymentItem item : payment.getItems()) {
                itemsTable.addCell(new Cell().add(new Paragraph(item.getDescription() != null ? item.getDescription() : "N/A").setFont(regularFont)));
                itemsTable.addCell(new Cell().add(new Paragraph(item.getPeriod() != null ? item.getPeriod() : "N/A").setFont(regularFont)));
                itemsTable.addCell(new Cell().add(new Paragraph("$" + item.getAmount()).setFont(regularFont)));
            }
            
            document.add(itemsTable);
            document.add(new Paragraph("\n"));
            
            // Footer
            document.add(new Paragraph("Este comprobante es válido como constancia de pago.")
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));
            
            document.add(new Paragraph("Fecha de emisión: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));
            
            // Close document
            document.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            // Fallback to text-based receipt if PDF generation fails
            return generateFallbackTextReceipt(payment);
        }
    }
    
    private void addTableRow(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(boldFont)));
        table.addCell(new Cell().add(new Paragraph(value).setFont(regularFont)));
    }
    
    private byte[] generateFallbackTextReceipt(Payment payment) {
        // Get member information
        Member member = memberRepository.findById(payment.getMemberId()).orElse(null);
        String memberName = member != null ? member.getName() + " " + member.getLastname() : "Miembro no encontrado";
        
        // Generate detailed text receipt as fallback
        StringBuilder content = new StringBuilder();
        content.append("GRUPO SCOUT JOSÉ HERNÁNDEZ\n");
        content.append("COMPROBANTE DE PAGO\n\n");
        content.append("==========================================\n");
        content.append("DATOS DEL PAGO\n");
        content.append("==========================================\n");
        content.append("Referencia: ").append(payment.getReferenceId() != null ? payment.getReferenceId() : "N/A").append("\n");
        content.append("Fecha: ").append(payment.getPaymentDate() != null ? payment.getPaymentDate() : "N/A").append("\n");
        content.append("Beneficiario: ").append(memberName).append("\n");
        content.append("Monto Total: $").append(payment.getAmount()).append("\n");
        content.append("Estado: ").append(payment.getStatus()).append("\n");
        content.append("Método: ").append(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A").append("\n\n");
        
        content.append("==========================================\n");
        content.append("CONCEPTOS PAGADOS\n");
        content.append("==========================================\n");
        
        for (PaymentItem item : payment.getItems()) {
            content.append("- ").append(item.getDescription() != null ? item.getDescription() : "N/A")
                   .append(" (").append(item.getPeriod() != null ? item.getPeriod() : "N/A").append("): $")
                   .append(item.getAmount()).append("\n");
        }
        
        content.append("\n==========================================\n");
        content.append("Este comprobante es válido como constancia de pago.\n");
        content.append("Fecha de emisión: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        content.append("==========================================\n");
        
        return content.toString().getBytes();
    }

    public PaymentStatisticsDto getPaymentStatistics(String dateFrom, String dateTo, Integer sectionId) {
        List<Object[]> results = paymentRepository.getPaymentStatistics(dateFrom, dateTo);
        
        if (results.isEmpty()) {
            return PaymentStatisticsDto.builder()
                    .totalPayments(0L)
                    .completedPayments(0L)
                    .pendingPayments(0L)
                    .failedPayments(0L)
                    .totalAmount(BigDecimal.ZERO)
                    .completedAmount(BigDecimal.ZERO)
                    .pendingAmount(BigDecimal.ZERO)
                    .averagePaymentAmount(BigDecimal.ZERO)
                    .build();
        }

        Object[] result = results.get(0);
        return PaymentStatisticsDto.builder()
                .totalPayments(((Number) result[0]).longValue())
                .completedPayments(((Number) result[1]).longValue())
                .pendingPayments(((Number) result[2]).longValue())
                .failedPayments(((Number) result[3]).longValue())
                .totalAmount(convertToBigDecimal(result[4]))
                .completedAmount(convertToBigDecimal(result[5]))
                .pendingAmount(convertToBigDecimal(result[6]))
                .averagePaymentAmount(convertToBigDecimal(result[7]))
                .build();
    }

    public List<PendingPaymentsBySectionDto> getPendingPaymentsBySection() {
        List<Object[]> results = paymentRepository.getPendingPaymentsBySection();
        
        return results.stream()
                .map(result -> PendingPaymentsBySectionDto.builder()
                        .sectionId(((Number) result[0]).intValue())
                        .sectionName((String) result[1])
                        .totalPendingFees(((Number) result[2]).longValue())
                        .totalPendingAmount(convertToBigDecimal(result[3]))
                        .membersWithPendingPayments(((Number) result[4]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private PaymentDto mapToPaymentDTO(Payment savedPayment) {
        PaymentDto dto = new PaymentDto();
        List<PaymentItemDTO> itemDTOS = new ArrayList<>();
        savedPayment.getItems().forEach(item -> {
            PaymentItemDTO temp = PaymentItemDTO.builder()
                    .description(item.getDescription())
                    .amount(item.getAmount())
                    .feeId(item.getFee().getId())
                    .period(item.getPeriod())
                    .build();
            itemDTOS.add(temp);
        });
        
        // Get member information
        Member member = memberRepository.findById(savedPayment.getMemberId()).orElse(null);
        
        dto.setId(savedPayment.getId());
        dto.setMemberId(savedPayment.getMemberId());
        dto.setMemberName(member != null ? member.getName() : "Miembro no encontrado");
        dto.setMemberLastName(member != null ? member.getLastname() : "");
        dto.setAmount(savedPayment.getAmount());
        dto.setPaymentDate(savedPayment.getPaymentDate());
        dto.setStatus(savedPayment.getStatus().name().toLowerCase());
        dto.setReferenceId(savedPayment.getReferenceId());
        dto.setPaymentMethod(savedPayment.getPaymentMethod());
        dto.setItems(itemDTOS);
        return dto;
    }

    private List<PaymentItem> createPaymentItemsFromFees(List<Integer> feeIds) {
        return feeRepository.findAllById(feeIds).stream()
                .map(fee -> {
                    return PaymentItem.builder()
                            .fee(fee)
                            .description(fee.getDescription())
                            .period(fee.getPeriod())
                            .amount(fee.getAmount())
                            .build();
                }).collect(Collectors.toList());
    }

    private String detectPaymentMethodType(Map<String, Object> paymentFormData) {
        // Check if we have the new Payment Brick structure
        if (paymentFormData.containsKey("formData")) {
            Map<String, Object> formData = (Map<String, Object>) paymentFormData.get("formData");
            if (formData != null && formData.containsKey("token")) {
                return "card"; // Credit/debit card payments have a token
            } else if (formData != null && formData.containsKey("payment_method_id")) {
                String paymentMethodId = formData.get("payment_method_id").toString();
                if (paymentMethodId.equals("pix") || paymentMethodId.equals("bank_transfer")) {
                    return "bank_transfer";
                } else if (paymentMethodId.contains("rapipago") || paymentMethodId.contains("pagofacil")) {
                    return "ticket";
                } else if (paymentMethodId.equals("account_money")) {
                    return "digital_wallet";
                }
            }
        }
        
        // Fallback for old Card Payment Brick structure (direct access)
        if (paymentFormData.containsKey("token")) {
            return "card"; // Credit/debit card payments have a token
        } else if (paymentFormData.containsKey("payment_method_id")) {
            String paymentMethodId = paymentFormData.get("payment_method_id").toString();
            if (paymentMethodId.equals("pix") || paymentMethodId.equals("bank_transfer")) {
                return "bank_transfer";
            } else if (paymentMethodId.contains("rapipago") || paymentMethodId.contains("pagofacil")) {
                return "ticket";
            } else if (paymentMethodId.equals("account_money")) {
                return "digital_wallet";
            }
        }
        
        return "card"; // Default to card for backward compatibility
    }
    
    private PaymentCreateRequest buildPaymentRequest(ProcessPaymentRequest request, String paymentMethodType) {
        Map<String, Object> topLevelData = request.getPaymentFormData();
        
        // Debug log to see what data is coming from frontend
        System.out.println("=== DEBUG: Payment Form Data ===");
        System.out.println("Payment Method Type: " + paymentMethodType);
        System.out.println("Top Level Keys: " + topLevelData.keySet());
        topLevelData.forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("=================================");
        
        // Extract the actual form data from the nested structure
        Map<String, Object> formData = (Map<String, Object>) topLevelData.get("formData");
        if (formData == null) {
            throw new RuntimeException("No se encontraron datos del formulario en la estructura de pago");
        }
        
        Map<String, Object> payerData = (Map<String, Object>) formData.get("payer");
        
        PaymentCreateRequest.PaymentCreateRequestBuilder builder = PaymentCreateRequest.builder()
                .transactionAmount(getTotalAmountFromFees(request.getFeeIds()))
                .description("Pago de cuotas")
                .payer(PaymentPayerRequest.builder()
                        .email(payerData != null ? payerData.get("email").toString() : "default@example.com")
                        .build());
        
        switch (paymentMethodType) {
            case "card":
                // Card payment - existing logic
                Object tokenObj = formData.get("token");
                Object installmentsObj = formData.get("installments");
                Object paymentMethodIdObj = formData.get("payment_method_id");
                
                if (tokenObj == null) {
                    throw new RuntimeException("Token es requerido para pagos con tarjeta");
                }
                if (paymentMethodIdObj == null) {
                    throw new RuntimeException("payment_method_id es requerido para pagos con tarjeta");
                }
                
                return builder
                        .token(tokenObj.toString())
                        .installments(installmentsObj != null ? 
                            Integer.parseInt(installmentsObj.toString()) : 1)
                        .paymentMethodId(paymentMethodIdObj.toString())
                        .build();
                        
            case "bank_transfer":
                // Bank transfer payment
                Object bankPaymentMethodObj = formData.get("payment_method_id");
                if (bankPaymentMethodObj == null) {
                    throw new RuntimeException("payment_method_id es requerido para transferencias bancarias");
                }
                return builder
                        .paymentMethodId(bankPaymentMethodObj.toString())
                        .build();
                        
            case "ticket":
                // Cash payment (Rapipago, PagoFácil, etc.)
                Object ticketPaymentMethodObj = formData.get("payment_method_id");
                if (ticketPaymentMethodObj == null) {
                    throw new RuntimeException("payment_method_id es requerido para pagos en efectivo");
                }
                return builder
                        .paymentMethodId(ticketPaymentMethodObj.toString())
                        .build();
                        
            case "digital_wallet":
                // MercadoPago account payment
                return builder
                        .paymentMethodId("account_money")
                        .build();
                        
            default:
                throw new RuntimeException("Método de pago no soportado: " + paymentMethodType);
        }
    }

    private PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.COMPLETED;
            case  "pending"  -> PaymentStatus.PENDING;
            case "in_process", "authorized" -> PaymentStatus.PROCESSING;
            default -> PaymentStatus.FAILED;
        };
    }

    private Integer getMemberIdFromFees(List<Integer> feeIds) {
        return feeRepository.findById(feeIds.getFirst())
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"))
                .getMember().getId();
    }

    private BigDecimal getTotalAmountFromFees(List<Integer> feeIds) {
        return feeRepository.findAllById(feeIds).stream()
                .map(Fee::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void updateEventRegistrationPaymentStatus(Fee fee, PaymentStatus paymentStatus, Integer paymentId) {
        // Check if this fee is for an event by looking for "Evento:" in the description
        if (fee.getDescription() != null && fee.getDescription().startsWith("Evento:")) {
            // Find event registration for this member and event
            // We'll need to parse the event date from the period field to find the right registration
            Optional<EventRegistration> registration = eventRegistrationRepository
                    .findByMemberIdOrderByRegistrationDateDesc(fee.getMember().getId())
                    .stream()
                    .filter(reg -> {
                        // This is a simple matching - ideally we should store eventId in Fee
                        // For now, we match by member and assume latest registration for paid events
                        return reg.getPaymentStatus() == null || 
                               reg.getPaymentStatus() == PaymentStatus.PENDING;
                    })
                    .findFirst();
                    
            if (registration.isPresent()) {
                EventRegistration eventRegistration = registration.get();
                eventRegistration.setPaymentStatus(paymentStatus);
                eventRegistration.setPaymentId(paymentId);
                eventRegistrationRepository.save(eventRegistration);
            }
        }
    }
    
    @Transactional
    public void updatePaymentStatusFromWebhook(String mercadoPagoPaymentId) {
        try {
            // Fetch payment details from MercadoPago
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(mercadoPagoPaymentId));
            
            // Find our payment record by MercadoPago reference
            Optional<Payment> paymentOpt = paymentRepository.findByReferenceId(mercadoPagoPaymentId);
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                PaymentStatus newStatus = mapMercadoPagoStatus(mpPayment.getStatus());
                
                // Only update if status actually changed
                if (payment.getStatus() != newStatus) {
                    payment.setStatus(newStatus);
                    // Note: PaymentMethod is an entity, not enum. We'd need to find or create the method
                    // For now, we'll skip updating payment method to avoid complexity
                    
                    // Update payment date when approved
                    if (newStatus == PaymentStatus.COMPLETED && payment.getPaymentDate() == null) {
                        // Convert OffsetDateTime to LocalDateTime and then to String
                        if (mpPayment.getDateApproved() != null) {
                            payment.setPaymentDate(mpPayment.getDateApproved().toString());
                        }
                    }
                    
                    paymentRepository.save(payment);
                    
                    // Update related fees status
                    updateFeesStatusFromWebhook(payment.getItems(), newStatus, payment.getId());
                    
                    // Update event registration status if applicable
                    for (PaymentItem item : payment.getItems()) {
                        if (item.getFee() != null) {
                            updateEventRegistrationPaymentStatus(item.getFee(), newStatus, payment.getId());
                        }
                    }
                    
                    System.out.println("Payment " + payment.getId() + " status updated to " + newStatus + " via webhook");
                }
            } else {
                System.out.println("Warning: Received webhook for unknown payment: " + mercadoPagoPaymentId);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing webhook for payment " + mercadoPagoPaymentId + ": " + e.getMessage());
            // Don't throw exception - we don't want to cause webhook retries for processing errors
        }
    }
    
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        try {
            // Implement webhook signature validation for security
            // This validates that the webhook actually came from MercadoPago
            
            // For now, return true if signature validation is not critical in development
            // In production, you should implement proper HMAC-SHA256 validation
            
            if (signature == null || signature.isEmpty()) {
                return false; // Reject requests without signature
            }
            
            // TODO: Implement HMAC-SHA256 validation with your webhook secret
            // String expectedSignature = calculateHMACSignature(payload, secret);
            // return signature.equals(expectedSignature);
            
            return true; // Temporarily accept all signed requests
            
        } catch (Exception e) {
            System.err.println("Error validating webhook signature: " + e.getMessage());
            return false;
        }
    }
    
    private void updateFeesStatusFromWebhook(List<PaymentItem> paymentItems, PaymentStatus paymentStatus, Integer paymentId) {
        for (PaymentItem item : paymentItems) {
            if (item.getFee() != null) {
                Fee fee = item.getFee();
                
                // Map payment status to fee status (both use PaymentStatus enum)
                PaymentStatus feeStatus = switch (paymentStatus) {
                    case COMPLETED -> PaymentStatus.COMPLETED;
                    case PENDING -> PaymentStatus.PENDING;
                    case REFUNDED -> null;
                    case PROCESSING -> PaymentStatus.PROCESSING;
                    case FAILED -> PaymentStatus.PENDING; // Keep as pending if payment failed
                    case UNKNOWN -> null;
                };
                
                fee.setStatus(feeStatus);
                feeRepository.save(fee);
            }
        }
    }
    
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
//    public PaymentResponseDto createPaymentPreference(PaymentRequestDto paymentRequestDto) throws MPException, MPApiException {
//        PreferenceClient client = new PreferenceClient();
//
//        // Generar UUID v4 para X-Idempotency-Key
//        String idempotencyKey = UUID.randomUUID().toString();
//
//        // Configurar el encabezado en el cliente
//        Map<String, String> headers = new HashMap<>();
//        headers.put("X-Idempotency-Key", idempotencyKey);
//
//        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
//
//                .build();
//
//        List<PreferenceItemRequest> items = new ArrayList<>();
//
//        paymentRequestDto.getItems().forEach(item -> {
//            PreferenceItemRequest preferenceItem = PreferenceItemRequest.builder()
//                    .title(item.getTitle())
//                    .description(item.getDescription())
//                    .quantity(item.getQuantity())
//                    .currencyId("ARS")
//                    .unitPrice(item.getUnitPrice())
//                    .build();
//            items.add(preferenceItem);
//        });
//        PreferenceBackUrlsRequest backUrlsRequest = PreferenceBackUrlsRequest.builder()
//                .success(frontendUrl + "/payment/success")
//                .pending(frontendUrl + "/payment/pending")
//                .failure(frontendUrl + "/payment/failure")
//                .build();
//        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
//                .items(items)
//                .backUrls(backUrlsRequest)
//                .externalReference(paymentRequestDto.getExternalReference())
//                .autoReturn("approved")
//                .build();
//
//        Preference preference = client.create(preferenceRequest, MPRequestOptions.builder()
//                .customHeaders(headers)
//                .build());
//
//        Payment payment = createPaymentRecord(
//                paymentRequestDto.getUserId(),
//                paymentRequestDto.getPaymentType(),
//                paymentRequestDto.getAmount(),
//                preference.getId(),
//                paymentRequestDto.getExternalReference()
//        );
//
//        PaymentResponseDto responseDTO = new PaymentResponseDto();
//        responseDTO.setPreferenceId(preference.getId());
//        responseDTO.setInitPoint(preference.getInitPoint());
//        responseDTO.setSandboxInitPoint(preference.getSandboxInitPoint());
//        responseDTO.setPaymentId(payment.getId());
//
//        return responseDTO;
//    }
//
//    @Transactional
//    public Payment createPaymentRecord(Integer userId, String paymentType, BigDecimal amount, String preferenceId, String externalReference) {
//        Payment payment = new Payment();
//        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
//
//        payment.setUser(user);
//        payment.setPaymentType(paymentType);
//        payment.setAmount(amount);
//        payment.setPreferenceId(preferenceId);
//        payment.setExternalReference(externalReference);
//        payment.setStatus(PaymentStatus.PENDING);
//        payment.setCreatedAt(LocalDateTime.now());
//
//        return paymentRepository.save(payment);
//    }

    /**
     * Get pending fees for admin with filtering and pagination
     */
    public Map<String, Object> getPendingFeesForAdmin(Map<String, Object> filters, int page, int limit) {
        // Safe casting with null checks
        String memberName = (String) filters.get("memberName");
        Integer sectionId = filters.get("sectionId") != null ? (Integer) filters.get("sectionId") : null;
        Integer familyGroupId = filters.get("familyGroupId") != null ? (Integer) filters.get("familyGroupId") : null;
        BigDecimal minAmount = filters.get("minAmount") != null ? (BigDecimal) filters.get("minAmount") : null;
        BigDecimal maxAmount = filters.get("maxAmount") != null ? (BigDecimal) filters.get("maxAmount") : null;
        String period = (String) filters.get("period");
        
        Page<Fee> pendingFeesPage = feeRepository.findPendingFeesForAdmin(
                memberName,
                sectionId,
                familyGroupId,
                minAmount,
                maxAmount,
                period,
                PageRequest.of(page, limit)
        );

        List<Map<String, Object>> feesWithMemberInfo = pendingFeesPage.getContent().stream()
                .map(fee -> {
                    Map<String, Object> feeInfo = new HashMap<>();
                    feeInfo.put("id", fee.getId());
                    feeInfo.put("description", fee.getDescription());
                    feeInfo.put("amount", fee.getAmount());
                    feeInfo.put("period", fee.getPeriod());
                    feeInfo.put("status", fee.getStatus());
                    
                    // Flat structure for easier frontend consumption
                    if (fee.getMember() != null) {
                        feeInfo.put("memberId", fee.getMember().getId());
                        feeInfo.put("memberName", fee.getMember().getName());
                        feeInfo.put("memberLastName", fee.getMember().getLastname());
                        feeInfo.put("sectionName", fee.getMember().getSection() != null ? fee.getMember().getSection().getDescription() : "Sin sección");
                        feeInfo.put("familyGroupName", fee.getMember().getFamilyGroup() != null ? fee.getMember().getFamilyGroup().getName() : "Sin familia");
                    } else {
                        feeInfo.put("memberId", null);
                        feeInfo.put("memberName", "Sin miembro");
                        feeInfo.put("memberLastName", "");
                        feeInfo.put("sectionName", "Sin sección");
                        feeInfo.put("familyGroupName", "Sin familia");
                    }
                    
                    return feeInfo;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("fees", feesWithMemberInfo);
        response.put("total", pendingFeesPage.getTotalElements());
        response.put("totalPages", pendingFeesPage.getTotalPages());
        response.put("currentPage", page);

        return response;
    }
}