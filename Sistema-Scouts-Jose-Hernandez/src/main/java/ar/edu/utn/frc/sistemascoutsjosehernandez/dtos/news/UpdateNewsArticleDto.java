package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateNewsArticleDto {
    
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    private String title;
    
    @Size(max = 1000, message = "El resumen no puede exceder 1000 caracteres")
    private String summary;
    
    private String content;
    
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String featuredImage;
    
    private Set<Integer> categoryIds;
}