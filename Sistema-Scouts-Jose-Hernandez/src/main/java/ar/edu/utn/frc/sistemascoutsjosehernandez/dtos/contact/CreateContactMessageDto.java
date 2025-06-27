package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new contact message from the website form
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateContactMessageDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 255, message = "El asunto no puede exceder 255 caracteres")
    private String subject;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 5000, message = "El mensaje no puede exceder 5000 caracteres")
    private String message;

    @NotNull(message = "El tipo de mensaje es obligatorio")
    @Builder.Default
    private ContactMessageType messageType = ContactMessageType.GENERAL;
}