package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class NewsCategoryDto {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private String color;
}