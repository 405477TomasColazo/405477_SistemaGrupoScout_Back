package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.ProgressionStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarchSheetDto {
    private Integer id;
    private Integer memberId;
    private String memberName;
    private String totem;
    private ProgressionStage progressionStage;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CompetenceProgressDto> competenceProgress;
}