package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDto {
    private Integer userId;
    private String name;
    private String lastName;
    private String dni;
    private String section;
    private LocalDate birthDate;
    private String notes;
}
