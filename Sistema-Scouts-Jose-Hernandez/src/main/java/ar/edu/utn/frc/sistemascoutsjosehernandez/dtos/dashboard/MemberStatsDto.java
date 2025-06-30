package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberStatsDto {
    private long manadaCount;
    private long unidadCount;
    private long caminantesCount;
    private long roversCount;
    private long totalEducators;
    private long totalTutors;
}