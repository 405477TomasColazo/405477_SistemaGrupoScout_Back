package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.EntryStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.OriginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingEntryDto {
    private Long id;
    private Integer entryNumber;
    private LocalDate entryDate;
    private String description;
    private OriginType originType;
    private String originReference;
    private EntryStatus status;
    private String createdByUserName;
    private LocalDateTime createdAt;
    private String modifiedByUserName;
    private LocalDateTime modifiedAt;
    private List<AccountingMovementDto> movements;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
}