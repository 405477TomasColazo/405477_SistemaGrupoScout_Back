package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.InvitationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private EventType eventType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private Set<String> sections;
    private BigDecimal cost;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private EventStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime registrationDeadline;
    private InvitationType invitationType;
    private Set<Integer> invitedMembers;
    private Boolean requiresPayment;
    private LocalDateTime paymentDeadline;
    private List<EventAttachmentDTO> attachments;
    private String notes;
}
