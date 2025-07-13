package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.notification;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UnreadNotificationCountResponse {
    private long count;
}