package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedActionDto {
    private Integer id;
    private String description;
    private Integer competenceId;
}