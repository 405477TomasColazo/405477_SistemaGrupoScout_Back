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

            Map<String, Object> payerData = (Map<String, Object>) request.getCardFormData().get("payer");

            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                    .transactionAmount(getTotalAmountFromFees(request.getFeeIds()))
                    .token(request.getCardFormData().get("token").toString())
                    .description("Pago de cuotas")
                    .installments(Integer.parseInt(request.getCardFormData().get("installments").toString()))
                    .paymentMethodId(request.getCardFormData().get("payment_method_id").toString())
                    .payer(PaymentPayerRequest.builder()
                            .email(payerData.get("email").toString())
                            .build())
                    .build();

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
                               reg.getPaymentStatus() == ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus.PENDING;
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