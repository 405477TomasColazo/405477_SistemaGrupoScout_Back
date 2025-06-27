package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events.EventDTO;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression.ProgressionStatsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EducatorDashboardDto {
    private String educatorName;
    private String sectionName;
    private long scoutsCount;
    private long pendingApprovals;
    private long upcomingEvents;
    private double progressionRate;
    private List<EventDTO> upcomingEventsList;
    private ProgressionStatsDto progressionStats;
}