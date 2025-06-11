package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TutorDto {
    private Integer id;
    private Integer userId;
    private String contactPhone;
    private String email;
    private String name;
    private String lastName;
    private LocalDate birthdate;
    private String dni;
    private String address;
    private String notes;
    private List<RelationshipDto> relationships;

}
