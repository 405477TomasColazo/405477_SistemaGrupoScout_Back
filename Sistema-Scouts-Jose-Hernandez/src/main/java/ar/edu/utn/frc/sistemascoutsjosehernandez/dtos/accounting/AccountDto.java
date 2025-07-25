package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String code;
    private String name;
    private AccountType type;
    private Boolean active;
    private String description;
}