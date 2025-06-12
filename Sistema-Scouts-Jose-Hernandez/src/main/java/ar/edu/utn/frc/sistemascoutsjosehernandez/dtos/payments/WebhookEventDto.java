package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebhookEventDto {
    private String id;
    private String type; // "payment", "plan", etc.
    private String action; // "payment.created", "payment.updated", etc.
    private LocalDateTime dateCreated;
    private Long userId;
    private String apiVersion;
    private WebhookDataDto data;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WebhookDataDto {
        private String id; // Payment ID or other resource ID
    }
}