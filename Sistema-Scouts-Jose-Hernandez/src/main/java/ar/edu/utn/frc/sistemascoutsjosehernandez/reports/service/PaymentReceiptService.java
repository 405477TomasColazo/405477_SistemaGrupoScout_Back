package ar.edu.utn.frc.sistemascoutsjosehernandez.reports.service;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Payment;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentItem;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.PaymentRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentReceiptService {

    private final ReportService reportService;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    private static final String PAYMENT_RECEIPT_TEMPLATE = "payment-receipt.jrxml";

    public PaymentReceiptService(ReportService reportService, 
                               PaymentRepository paymentRepository,
                               MemberRepository memberRepository) {
        this.reportService = reportService;
        this.paymentRepository = paymentRepository;
        this.memberRepository = memberRepository;
    }

    public byte[] generatePaymentReceipt(Integer paymentId) {
        log.info("Generating payment receipt for payment ID: {}", paymentId);

        // Fetch payment data
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        // Fetch member data
        Member member = memberRepository.findById(payment.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + payment.getMemberId()));

        // Prepare report parameters
        Map<String, Object> parameters = prepareReceiptParameters(payment, member);

        // Prepare payment items data
        List<PaymentItemData> paymentItems = preparePaymentItemsData(payment.getItems());
        parameters.put("paymentItems", new JRBeanCollectionDataSource(paymentItems));

        // Create a minimal datasource to trigger the detail band
        List<Map<String, Object>> dummyData = List.of(Map.of("dummy", "dummy"));
        
        // Generate the report
        byte[] result = reportService.generateReport(PAYMENT_RECEIPT_TEMPLATE, parameters, dummyData);
        log.info("Payment receipt generated successfully. Size: {} bytes", result.length);
        
        return result;
    }

    private Map<String, Object> prepareReceiptParameters(Payment payment, Member member) {
        Map<String, Object> parameters = new HashMap<>();

        // Payment information
        parameters.put("paymentId", payment.getId());
        parameters.put("amount", payment.getAmount());
        parameters.put("paymentDate", payment.getPaymentDate() != null ? payment.getPaymentDate() : "Fecha no disponible");
        parameters.put("paymentMethod", translatePaymentMethod(payment.getPaymentMethod()));
        parameters.put("referenceId", payment.getReferenceId() != null ? payment.getReferenceId() : "N/A");

        // Member information
        parameters.put("memberName", member.getName() + " " + member.getLastname());
        parameters.put("memberDni", member.getDni());
        parameters.put("familyName", member.getFamilyGroup().getName());
        parameters.put("sectionName", member.getSection() != null ? member.getSection().getDescription() : "No asignado");

        log.debug("Receipt parameters prepared for payment: {}, member: {}", 
                 payment.getId(), member.getName() + " " + member.getLastname());

        return parameters;
    }

    private List<PaymentItemData> preparePaymentItemsData(List<PaymentItem> paymentItems) {
        if (paymentItems == null || paymentItems.isEmpty()) {
            log.warn("No payment items found for receipt generation");
            return List.of();
        }

        return paymentItems.stream()
            .map(item -> PaymentItemData.builder()
                .description(item.getDescription())
                .amount(item.getAmount())
                .feeType(item.getFee() != null && item.getFee().getPeriod() != null ? 
                        translateFeeType(item.getFee().getPeriod()) : "Pago único")
                .build())
            .collect(Collectors.toList());
    }

//    private String translatePaymentMethod(String paymentMethod) {
//        if (paymentMethod == null) return "No especificado";
//
//        return switch (paymentMethod.toLowerCase()) {
//            case "credit_card" -> "Tarjeta de Crédito";
//            case "debit_card" -> "Tarjeta de Débito";
//            case "mercadopago" -> "MercadoPago";
//            case "cash" -> "Efectivo";
//            case "bank_transfer" -> "Transferencia Bancaria";
//            default -> paymentMethod;
//        };
//
//    }

    private String translatePaymentMethod(String paymentMethod) {
        if (paymentMethod == null) return "N/A";

        String method = paymentMethod.toLowerCase();

        // Tipos de pago detectados
        switch (method) {
            case "card":
                return "Tarjeta de crédito/débito";
            case "ticket":
                return "Efectivo";
            case "bank_transfer":
                return "Transferencia bancaria";
            case "digital_wallet":
                return "Billetera digital";
            case "account_money":
                return "Dinero en cuenta";
            case "account_balance":
                return "Balance de cuenta";
            case "atm":
                return "Cajero automático";

            // IDs específicos de MercadoPago (para pagos ya existentes)
            case "visa":
                return "Tarjeta Visa";
            case "mastercard":
            case "master":
                return "Tarjeta Mastercard";
            case "american_express":
            case "amex":
                return "Tarjeta American Express";
            case "credit_card":
                return "Tarjeta de crédito";
            case "debit_card":
                return "Tarjeta de débito";
            case "pix":
                return "PIX";
            case "rapipago":
                return "RapiPago";
            case "pagofacil":
                return "PagoFácil";
            case "bacs":
                return "Transferencia bancaria";
            case "wire_transfer":
                return "Transferencia bancaria";

            // Para casos no reconocidos, intentar identificar el tipo
            default:
                // Si contiene ciertos términos, intentar clasificar
                if (method.contains("card") || method.contains("visa") || method.contains("master") ||
                        method.contains("amex") || method.contains("credit") || method.contains("debit")) {
                    return "Tarjeta de crédito/débito";
                }
                if (method.contains("bank") || method.contains("transfer") || method.contains("wire")) {
                    return "Transferencia bancaria";
                }
                if (method.contains("cash") || method.contains("efectivo") || method.contains("ticket")) {
                    return "Efectivo";
                }

                // Si no podemos identificar, retornar el valor original formateado
                return paymentMethod.substring(0, 1).toUpperCase() + paymentMethod.substring(1).toLowerCase();
        }
    }

    private String translateFeeType(String period) {
        if (period == null) return "Único";
        
        return switch (period.toLowerCase()) {
            case "monthly" -> "Mensual";
            case "quarterly" -> "Trimestral";
            case "annual" -> "Anual";
            case "one_time" -> "Único";
            default -> period;
        };
    }


    public boolean canGenerateReceipt(Integer paymentId) {
        if (paymentId == null) {
            return false;
        }

        try {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null || payment.getMemberId() == null) {
                return false;
            }

            Member member = memberRepository.findById(payment.getMemberId()).orElse(null);
            return member != null && reportService.templateExists(PAYMENT_RECEIPT_TEMPLATE);
            
        } catch (Exception e) {
            log.error("Error checking if receipt can be generated for payment: {}", paymentId, e);
            return false;
        }
    }

    @Data
    @lombok.Builder
    public static class PaymentItemData {
        private String description;
        private BigDecimal amount;
        private String feeType;
    }
}