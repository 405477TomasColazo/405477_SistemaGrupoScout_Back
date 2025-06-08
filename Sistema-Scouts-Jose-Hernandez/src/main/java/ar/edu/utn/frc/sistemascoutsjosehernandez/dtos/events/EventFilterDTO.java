package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFilterDTO {
    private Set<String> sections;
    private EventType eventType;
    private EventStatus status;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Long createdBy;
}
