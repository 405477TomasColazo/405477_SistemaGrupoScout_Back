package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Role;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
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
    private String avatar;
    private Instant createdAt;
    private Instant lastLogin;
    private List<RoleDto> roles;
    
    // Mapper method for simple user data (without roles)
    public static UserDto fromEntity(User entity) {
        if (entity == null) {
            return null;
        }
        
        return UserDto.builder()
            .id(entity.getId())
            .email(entity.getEmail())
            .lastName(entity.getLastName())
            .avatar(entity.getAvatar())
            .createdAt(entity.getCreatedAt())
            .lastLogin(entity.getLastLogin())
            .build();
    }
}
