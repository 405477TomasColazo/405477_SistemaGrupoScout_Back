package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.ProgressionStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressionStageDto {
    @NotNull(message = "El memberId es requerido")
    private Integer memberId;
    
    @NotNull(message = "La nueva etapa es requerida")
    private ProgressionStage newStage;
    
    private String comments;
}