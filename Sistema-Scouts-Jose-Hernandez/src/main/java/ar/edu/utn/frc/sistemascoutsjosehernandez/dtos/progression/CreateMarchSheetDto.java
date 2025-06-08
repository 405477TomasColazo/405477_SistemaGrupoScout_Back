package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.ProgressionStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMarchSheetDto {
    @NotNull
    private Integer memberId;
    
    private String totem;
    
    @NotNull
    private ProgressionStage progressionStage;
    
    private List<Integer> selectedCompetenceIds;
}