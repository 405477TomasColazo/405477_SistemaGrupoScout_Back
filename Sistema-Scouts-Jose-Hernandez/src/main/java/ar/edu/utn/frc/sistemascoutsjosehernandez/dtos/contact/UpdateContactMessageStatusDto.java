package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating contact message status and admin notes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateContactMessageStatusDto {

    @NotNull(message = "El estado es obligatorio")
    private ContactMessageStatus status;

    @Size(max = 2000, message = "Las notas del administrador no pueden exceder 2000 caracteres")
    private String adminNotes;
}