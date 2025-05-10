package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionMemberDto {
    private Integer id;
    private Integer userId;
    private String name;
    private String lastName;
    private String dni;
    private String section;
    private LocalDate birthdate;
    private String notes;
    private List<RelationshipDto> relationships;
    private BigDecimal accountBalance;
    @JsonProperty("isEducator")
    private boolean isEducator;
    private String address;
}
