package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewUserDto {
    private String email;
    private String lastName;
    private String userType; // "FAMILY" or "EDUCATOR"
    private Integer sectionId; // Only for educators
}
