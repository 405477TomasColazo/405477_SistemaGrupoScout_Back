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
public class TutorDto {
    private Integer userId;
    private String contactPhone;
    private String email;
    private String name;
    private String lastName;
    private LocalDate birthDate;
    private String dni;
    private String notes;
}
