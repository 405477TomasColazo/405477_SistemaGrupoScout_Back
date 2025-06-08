package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.InvitationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateEventDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotBlank(message = "Location is required")
    private String location;

    @NotEmpty(message = "At least one section is required")
    private Set<String> sections;

    private BigDecimal cost;
    private Integer maxCapacity;
    private EventStatus status = EventStatus.DRAFT;

    @NotNull(message = "Created by is required")
    private Long createdBy;

    private LocalDateTime registrationDeadline;
    private InvitationType invitationType = InvitationType.ALL;
    private Set<Integer> invitedMembers;
    private Boolean requiresPayment = false;
    private LocalDateTime paymentDeadline;
    private String notes;
}
