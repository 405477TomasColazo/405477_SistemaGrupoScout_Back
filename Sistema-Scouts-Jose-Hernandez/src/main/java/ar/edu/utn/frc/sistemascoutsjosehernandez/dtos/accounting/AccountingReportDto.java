package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingReportDto {
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal balanceDifference;
    private List<AccountingEntryDto> entries;
    private List<AccountBalanceDto> accountBalances;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountBalanceDto {
        private Long accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal totalDebits;
        private BigDecimal totalCredits;
        private BigDecimal balance;
    }
}