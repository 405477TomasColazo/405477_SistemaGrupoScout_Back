package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.notification;

import lombok.Data;
import lombok.Builder;

import java.time.Instant;

@Data
@Builder
public class NotificationDto {
    private Integer id;
    private String title;
    private String message;
    private String notificationType;
    private Instant createdAt;
    private Instant readAt;
    private String relatedEntity;
    private Integer relatedEntityId;
    private boolean read;
}