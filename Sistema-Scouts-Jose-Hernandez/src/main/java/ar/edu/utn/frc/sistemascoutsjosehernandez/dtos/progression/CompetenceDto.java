package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.GrowthArea;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenceDto {
    private Integer id;
    private String title;
    private String description;
    private GrowthArea growthArea;
    private Integer sectionId;
    private String sectionName;
    private List<SuggestedActionDto> suggestedActions;
    private List<String> guidingQuestions;
}