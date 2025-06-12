package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateNewsArticleDto {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    private String title;
    
    @NotBlank(message = "El resumen es obligatorio")
    @Size(max = 1000, message = "El resumen no puede exceder 1000 caracteres")
    private String summary;
    
    @NotBlank(message = "El contenido es obligatorio")
    private String content;
    
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String featuredImage;
    
    private Set<Integer> categoryIds;
}