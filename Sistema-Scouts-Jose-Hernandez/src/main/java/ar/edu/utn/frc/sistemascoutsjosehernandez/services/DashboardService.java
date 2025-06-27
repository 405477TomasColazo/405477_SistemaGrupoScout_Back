package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events.EventDTO;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.payments.FeeDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression.ProgressionStatsDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.Event;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.progression.ProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MemberRepository memberRepository;
    private final EducatorRepository educatorRepository;
    private final FamilyGroupRepository familyGroupRepository;
    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final FeeRepository feeRepository;
    private final EventService eventService;
    private final PaymentService paymentService;
    private final ProgressionService progressionService;

    public DashboardStatsDto getAdminDashboardStats() {
        MemberStatsDto memberStats = getMemberStats();
        EventStatsDto eventStats = getEventStats();
        FinancialStatsDto financialStats = getFinancialStats();

        return DashboardStatsDto.builder()
                .totalScouts(memberStats.getManadaCount() + memberStats.getUnidadCount() + 
                           memberStats.getCaminantesCount() + memberStats.getRoversCount())
                .totalFamilies(familyGroupRepository.count())
                .activeEvents(eventStats.getActiveEvents())
                .monthlyRevenue(financialStats.getMonthlyRevenue())
                .memberStats(memberStats)
                .eventStats(eventStats)
                .financialStats(financialStats)
                .build();
    }

    public EducatorDashboardDto getEducatorDashboardStats(Integer userId) {
        Optional<Educator> educatorOpt = educatorRepository.findByUser_Id(userId);
        if (educatorOpt.isEmpty()) {
            throw new RuntimeException("Educator not found");
        }

        Educator educator = educatorOpt.get();
        Section section = educator.getSection();

        // Get scouts count in section (excluding tutors)
        long scoutsCount = memberRepository.findAllByIsActiveTrueAndSection(section).size();

        // Get upcoming events for section
        List<Event> upcomingEvents = eventRepository.findEventsWithFilters(
                null, null, EventStatus.PUBLISHED, LocalDateTime.now(), null, null
        ).stream()
         .filter(event -> event.getSections().contains(section.getDescription()))
         .collect(Collectors.toList());

        // Get progression stats - we'll need to get pending approvals count
        long pendingApprovals = 0; // TODO: Implement based on progression service
        
        // Calculate progression rate for section
        double progressionRate = calculateSectionProgressionRate(section);

        return EducatorDashboardDto.builder()
                .educatorName(educator.getName() + " " + educator.getLastname())
                .sectionName(section.getDescription())
                .scoutsCount(scoutsCount)
                .pendingApprovals(pendingApprovals)
                .upcomingEvents(upcomingEvents.size())
                .progressionRate(progressionRate)
                .upcomingEventsList(upcomingEvents.stream()
                        .limit(5)
                        .map(eventService::convertToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public FamilyDashboardDto getFamilyDashboardStats(Integer userId) {
        FamilyGroup familyGroupOpt = familyGroupRepository.findFamilyGroupsByUser_Id(userId);
        if (familyGroupOpt == null) {
            throw new RuntimeException("Family group not found");
        }

        FamilyGroup familyGroup = familyGroupOpt;
        
        // Get active scouts in family
        List<Member> familyMembers = memberRepository.findAllByIsActiveTrueAndFamilyGroup_Id(familyGroup.getId());
        long scoutsInFamily = familyMembers.stream()
                .filter(member -> !member.getIsTutor())
                .count();

        // Get upcoming events for family
        List<Event> upcomingEvents = eventRepository.findEventsWithFilters(
                null, null, EventStatus.PUBLISHED, LocalDateTime.now(), null, null
        );

        // Get pending payments for family members
        List<Fee> pendingFees = familyMembers.stream()
                .flatMap(member -> feeRepository.findByMemberIdAndStatus(member.getId(), PaymentStatus.PENDING).stream())
                .collect(Collectors.toList());

        BigDecimal totalPendingAmount = pendingFees.stream()
                .map(Fee::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FeeDto> pendingFeesList = pendingFees.stream()
                .map(this::convertToFeeDto)
                .collect(Collectors.toList());

        return FamilyDashboardDto.builder()
                .familyName(familyGroup.getUser().getLastName())
                .scoutsInFamily(scoutsInFamily)
                .upcomingEvents(upcomingEvents.size())
                .pendingPayments(pendingFees.size())
                .totalPendingAmount(totalPendingAmount)
                .upcomingEventsList(upcomingEvents.stream()
                        .limit(5)
                        .map(eventService::convertToDTO)
                        .collect(Collectors.toList()))
                .pendingFeesList(pendingFeesList)
                .build();
    }

    private MemberStatsDto getMemberStats() {
        // Count by sections - this assumes section IDs are standardized
        long manadaCount = countMembersBySection("Manada");
        long unidadCount = countMembersBySection("Unidad");
        long caminantesCount = countMembersBySection("Caminantes");
        long roversCount = countMembersBySection("Rovers");
        
        long totalEducators = educatorRepository.count();
        long totalTutors = memberRepository.findActiveTutors().size();

        return MemberStatsDto.builder()
                .manadaCount(manadaCount)
                .unidadCount(unidadCount)
                .caminantesCount(caminantesCount)
                .roversCount(roversCount)
                .totalEducators(totalEducators)
                .totalTutors(totalTutors)
                .build();
    }

    private EventStatsDto getEventStats() {
        long totalEvents = eventRepository.count();
        long activeEvents = eventRepository.findByStatusOrderByStartDateAsc(EventStatus.PUBLISHED).size();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusMonths(3);
        List<Event> upcomingEvents = eventRepository.findByDateRange(now, futureDate);
        
        LocalDateTime pastDate = now.minusMonths(6);
        List<Event> pastEvents = eventRepository.findByDateRange(pastDate, now);

        List<Event> recentEvents = eventRepository.findByDateRange(now.minusWeeks(2), now);

        return EventStatsDto.builder()
                .totalEvents(totalEvents)
                .activeEvents(activeEvents)
                .upcomingEvents(upcomingEvents.size())
                .pastEvents(pastEvents.size())
                .recentEvents(recentEvents.stream()
                        .map(eventService::convertToDTO)
                        .collect(Collectors.toList()))
                .upcomingEventsList(upcomingEvents.stream()
                        .limit(5)
                        .map(eventService::convertToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private FinancialStatsDto getFinancialStats() {
        // Get current month range
        LocalDate now = LocalDate.now();
        String monthStart = now.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String monthEnd = now.withDayOfMonth(now.lengthOfMonth()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Get payment statistics
        List<Object[]> stats = paymentRepository.getPaymentStatistics(null, null);
        List<Object[]> monthlyStats = paymentRepository.getPaymentStatistics(monthStart, monthEnd);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        long totalPaymentCount = 0;
        long completedPaymentCount = 0;
        long pendingPaymentCount = 0;

        if (!stats.isEmpty()) {
            Object[] row = stats.get(0);
            totalPaymentCount = ((Number) row[0]).longValue();
            completedPaymentCount = ((Number) row[1]).longValue();
            pendingPaymentCount = ((Number) row[2]).longValue();
            totalRevenue = (BigDecimal) row[5]; // completed amount
        }

        if (!monthlyStats.isEmpty()) {
            Object[] row = monthlyStats.get(0);
            monthlyRevenue = (BigDecimal) row[5]; // completed amount for this month
        }

        // Get pending payments amount
        BigDecimal pendingPayments = BigDecimal.ZERO;
        List<Fee> allPendingFees = feeRepository.findByStatus(PaymentStatus.PENDING);
        if (!allPendingFees.isEmpty()) {
            pendingPayments = allPendingFees.stream()
                    .map(Fee::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return FinancialStatsDto.builder()
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .pendingPayments(pendingPayments)
                .totalPaymentCount(totalPaymentCount)
                .pendingPaymentCount(pendingPaymentCount)
                .completedPaymentCount(completedPaymentCount)
                .build();
    }

    private long countMembersBySection(String sectionName) {
        return memberRepository.findAllByIsActiveTrueAndIsTutor(false)
                .stream()
                .filter(member -> member.getSection() != null && 
                        member.getSection().getDescription().equalsIgnoreCase(sectionName))
                .count();
    }

    private double calculateSectionProgressionRate(Section section) {
        // This would need to be implemented with the progression service
        // For now, return a placeholder
        return 75.0;
    }

    private FeeDto convertToFeeDto(Fee fee) {
        return FeeDto.builder()
                .id(fee.getId())
                .description(fee.getDescription())
                .amount(fee.getAmount())
                .period(fee.getPeriod())
                .memberId(fee.getMember().getId())
                .status(fee.getStatus().name().toLowerCase())
                .build();
    }
}