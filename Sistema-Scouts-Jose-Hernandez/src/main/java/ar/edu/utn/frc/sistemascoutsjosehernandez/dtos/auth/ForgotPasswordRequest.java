package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequest {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;
}