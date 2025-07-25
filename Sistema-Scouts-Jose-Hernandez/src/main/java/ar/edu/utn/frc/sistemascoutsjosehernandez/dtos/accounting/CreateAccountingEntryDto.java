package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountingEntryDto {
    
    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @NotEmpty(message = "At least one movement is required")
    @Valid
    private List<CreateAccountingMovementDto> movements;
}