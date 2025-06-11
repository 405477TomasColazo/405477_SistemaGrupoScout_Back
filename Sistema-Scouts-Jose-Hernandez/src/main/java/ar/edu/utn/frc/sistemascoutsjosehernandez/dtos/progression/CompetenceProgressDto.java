package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.CompetenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenceProgressDto {
    private Integer id;
    private Integer competenceId;
    private CompetenceDto competence;
    private Integer marchSheetId;
    private String ownAction;
    private String achievement;
    private LocalDate startDate;
    private LocalDate completionDate;
    private CompetenceStatus status;
    private Integer approvedByEducatorId;
    private String approvedByEducatorName;
    private LocalDate approvalDate;
    private String educatorComments;
    private Instant createdAt;
    private Instant updatedAt;
}