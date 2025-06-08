package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.CompetenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompetenceProgressDto {
    private String ownAction;
    private String achievement;
    private LocalDate completionDate;
    private CompetenceStatus status;
}