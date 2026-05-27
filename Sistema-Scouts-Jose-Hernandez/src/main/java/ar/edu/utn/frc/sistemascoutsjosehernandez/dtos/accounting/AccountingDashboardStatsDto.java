package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingDashboardStatsDto {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalEntries;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal balanceDifference;
    private Integer accountsWithMovements;
    private Integer automaticEntries;
    private Integer manualEntries;
}