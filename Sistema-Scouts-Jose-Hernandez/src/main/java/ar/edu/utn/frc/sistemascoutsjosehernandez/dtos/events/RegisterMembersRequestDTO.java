package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterMembersRequestDTO {
    @NotEmpty(message = "Member IDs cannot be empty")
    private List<Integer> memberIds;
}
