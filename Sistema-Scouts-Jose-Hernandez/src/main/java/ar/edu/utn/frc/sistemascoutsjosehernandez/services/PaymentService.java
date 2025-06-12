package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Fee;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Payment;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentItem;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventRegistration;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.EventRegistrationRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FeeRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.PaymentRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.UserRepository;

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
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${token.mp}")
    private String mercadoPagoAccessToken;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final PaymentRepository paymentRepository;
    //private final UserRepository userRepository;
    private final FeeRepository feeRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

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

        Page<Payment> paymentsPage = paymentRepository.findByFilters(
                filters.getMemberId(),
                filters.getDateFrom(),
                filters.getDateTo(),
                filters.getMinAmount(),
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
        dto.setId(savedPayment.getId());
        dto.setMemberId(savedPayment.getMemberId());
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