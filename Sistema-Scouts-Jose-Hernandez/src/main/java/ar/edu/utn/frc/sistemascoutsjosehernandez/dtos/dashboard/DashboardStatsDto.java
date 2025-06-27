package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression.ProgressionStatsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalScouts;
    private long totalFamilies;
    private long activeEvents;
    private BigDecimal monthlyRevenue;
    private MemberStatsDto memberStats;
    private EventStatsDto eventStats;
    private FinancialStatsDto financialStats;
    private ProgressionStatsDto progressionStats;
}