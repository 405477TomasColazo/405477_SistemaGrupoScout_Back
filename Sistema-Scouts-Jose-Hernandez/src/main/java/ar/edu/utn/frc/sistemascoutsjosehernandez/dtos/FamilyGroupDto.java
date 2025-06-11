package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FamilyGroupDto {
    private Integer id;
    private UserDto user;
    private TutorDto mainContact;
    private List<TutorDto> tutors;
    private List<MemberDto> members;
}
