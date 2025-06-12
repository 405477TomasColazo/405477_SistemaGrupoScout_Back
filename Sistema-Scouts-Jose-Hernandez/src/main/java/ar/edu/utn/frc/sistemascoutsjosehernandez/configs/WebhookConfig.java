package ar.edu.utn.frc.sistemascoutsjosehernandez.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "webhook")
@Data
public class WebhookConfig {
    
    /**
     * Secret key for validating MercadoPago webhook signatures
     * This should be configured in application.properties as:
     * webhook.mercadopago.secret=your-secret-key
     */
    private MercadoPagoConfig mercadopago = new MercadoPagoConfig();
    
    @Data
    public static class MercadoPagoConfig {
        private String secret = "your-webhook-secret";
        private boolean validateSignature = false; // Set to true in production
        private String[] allowedActions = {"payment.created", "payment.updated"};
    }
}