package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvitationDto {
    private Integer id;
    private String email;
    private String lastName;
    private String userType; // "FAMILY" or "EDUCATOR"
    private String status;
    private LocalDateTime sentDate;
    private String sectionName; // Only for educators
}