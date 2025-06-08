package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.GrowthArea;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressionStatsDto {
    private Integer totalCompetences;
    private Integer completed;
    private Integer inProgress;
    private Integer pending;
    private Double generalPercentage;
    private Map<GrowthArea, AreaStatsDto> byArea;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaStatsDto {
        private Integer total;
        private Integer completed;
        private Double percentage;
    }
}