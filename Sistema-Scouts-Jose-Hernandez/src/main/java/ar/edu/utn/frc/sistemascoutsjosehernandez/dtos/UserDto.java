package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Integer id;
    private String email;
    private String lastName;
    private Instant createdAt;
    private Instant lastLogin;
    private List<RoleDto> roles;
}
