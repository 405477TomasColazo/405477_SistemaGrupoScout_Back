package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingMovementDto {
    private Long id;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private MovementType movementType;
    private BigDecimal amount;
    private String description;
}