package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.EntryStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.OriginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingFiltersDto {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private EntryStatus status;
    private OriginType originType;
    private String description;
    private Long accountId;
}