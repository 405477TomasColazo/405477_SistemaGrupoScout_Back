package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.TutorDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    private String email;
    private String password;
    private String lastName;
    private TutorDto tutor;
}
