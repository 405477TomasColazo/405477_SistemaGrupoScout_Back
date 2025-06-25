package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;
    
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, max = 255, message = "La contraseña debe tener entre 6 y 255 caracteres")
    private String newPassword;
    
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmPassword;
}