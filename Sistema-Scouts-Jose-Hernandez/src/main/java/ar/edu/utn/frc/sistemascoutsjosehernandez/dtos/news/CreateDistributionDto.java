package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.DistributionFilter;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateDistributionDto {
    
    @NotEmpty(message = "Debe especificar al menos un filtro")
    private List<FilterDto> filters;
    
    @Data
    public static class FilterDto {
        private DistributionFilter.FilterType filterType;
        private String filterValue;
    }
}