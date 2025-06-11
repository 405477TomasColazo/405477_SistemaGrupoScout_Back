package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRegistrationDTO {
    private Integer id;
    private Integer eventId;
    private Integer memberId;
    private String memberName;
    private String memberLastName;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;
    private PaymentStatus paymentStatus;
    private Integer paymentId;
    private String notes;
}
