package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplyBalanceRequest {
    private Integer memberId;
    private List<Integer> feeIds;
}