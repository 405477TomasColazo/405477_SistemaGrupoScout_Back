package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events.EventDTO;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments.FeeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FamilyDashboardDto {
    private String familyName;
    private long scoutsInFamily;
    private long upcomingEvents;
    private long pendingPayments;
    private BigDecimal totalPendingAmount;
    private List<EventDTO> upcomingEventsList;
    private List<FeeDto> pendingFeesList;
}