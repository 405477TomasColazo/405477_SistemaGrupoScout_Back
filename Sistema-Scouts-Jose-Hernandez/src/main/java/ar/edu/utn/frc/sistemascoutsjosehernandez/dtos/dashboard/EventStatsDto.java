package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events.EventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventStatsDto {
    private long totalEvents;
    private long activeEvents;
    private long upcomingEvents;
    private long pastEvents;
    private List<EventDTO> recentEvents;
    private List<EventDTO> upcomingEventsList;
}