package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

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
public class MemberDto {
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
    private String memberType;
    private String address;
    private String email;
    private String contactPhone;
}
