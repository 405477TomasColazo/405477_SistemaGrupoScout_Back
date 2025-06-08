package ar.edu.utn.frc.sistemascoutsjosehernandez.services.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.CompetenceProgressRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.CompetenceRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.MarchSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressionService {

    private final MarchSheetRepository marchSheetRepository;
    private final CompetenceProgressRepository competenceProgressRepository;
    private final CompetenceRepository competenceRepository;
    private final MemberRepository memberRepository;
    private final CompetenceService competenceService;

    @Transactional(readOnly = true)
    public Optional<MarchSheetDto> getMarchSheetByMember(Integer memberId) {
        return marchSheetRepository.findByMemberId(memberId)
                .map(this::convertMarchSheetToDto);
    }

    @Transactional(readOnly = true)
    public List<CompetenceProgressDto> getCompetenceProgressByMember(Integer memberId) {
        return competenceProgressRepository.findByMarchSheetMemberId(memberId)
                .stream()
                .map(this::convertCompetenceProgressToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProgressionStatsDto getProgressionStats(Integer memberId) {
        List<CompetenceProgress> progressList = competenceProgressRepository.findByMarchSheetMemberId(memberId);
        return calculateProgressionStats(progressList);
    }

    public MarchSheetDto createMarchSheet(CreateMarchSheetDto createDto) {
        Member member = memberRepository.findById(createDto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado con ID: " + createDto.getMemberId()));

        if (marchSheetRepository.existsByMemberId(createDto.getMemberId())) {
            throw new RuntimeException("Ya existe una hoja de marcha para este miembro");
        }

        MarchSheet marchSheet = MarchSheet.builder()
                .member(member)
                .totem(createDto.getTotem())
                .progressionStage(createDto.getProgressionStage())
                .build();

        MarchSheet savedMarchSheet = marchSheetRepository.save(marchSheet);

        if (createDto.getSelectedCompetenceIds() != null && !createDto.getSelectedCompetenceIds().isEmpty()) {
            addCompetencesToMarchSheet(savedMarchSheet.getId(), createDto.getSelectedCompetenceIds());
        }

        return convertMarchSheetToDto(savedMarchSheet);
    }

    public MarchSheetDto updateMarchSheet(Integer marchSheetId, CreateMarchSheetDto updateDto) {
        MarchSheet marchSheet = marchSheetRepository.findById(marchSheetId)
                .orElseThrow(() -> new RuntimeException("Hoja de marcha no encontrada con ID: " + marchSheetId));

        marchSheet.setTotem(updateDto.getTotem());
        marchSheet.setProgressionStage(updateDto.getProgressionStage());

        MarchSheet savedMarchSheet = marchSheetRepository.save(marchSheet);
        return convertMarchSheetToDto(savedMarchSheet);
    }

    public CompetenceProgressDto addCompetenceToMarchSheet(Integer marchSheetId, Integer competenceId) {
        MarchSheet marchSheet = marchSheetRepository.findById(marchSheetId)
                .orElseThrow(() -> new RuntimeException("Hoja de marcha no encontrada con ID: " + marchSheetId));

        Competence competence = competenceRepository.findById(competenceId)
                .orElseThrow(() -> new RuntimeException("Competencia no encontrada con ID: " + competenceId));

        if (competenceProgressRepository.existsByMarchSheetIdAndCompetenceId(marchSheetId, competenceId)) {
            throw new RuntimeException("La competencia ya está agregada a esta hoja de marcha");
        }

        CompetenceProgress progress = CompetenceProgress.builder()
                .competence(competence)
                .marchSheet(marchSheet)
                .status(CompetenceStatus.PENDING)
                .startDate(LocalDate.now())
                .build();

        CompetenceProgress savedProgress = competenceProgressRepository.save(progress);
        return convertCompetenceProgressToDto(savedProgress);
    }

    public void addCompetencesToMarchSheet(Integer marchSheetId, List<Integer> competenceIds) {
        for (Integer competenceId : competenceIds) {
            if (!competenceProgressRepository.existsByMarchSheetIdAndCompetenceId(marchSheetId, competenceId)) {
                addCompetenceToMarchSheet(marchSheetId, competenceId);
            }
        }
    }

    public CompetenceProgressDto updateCompetenceProgress(Integer progressId, UpdateCompetenceProgressDto updateDto) {
        CompetenceProgress progress = competenceProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Progreso de competencia no encontrado con ID: " + progressId));

        if (updateDto.getOwnAction() != null) {
            progress.setOwnAction(updateDto.getOwnAction());
        }
        if (updateDto.getAchievement() != null) {
            progress.setAchievement(updateDto.getAchievement());
        }
        if (updateDto.getCompletionDate() != null) {
            progress.setCompletionDate(updateDto.getCompletionDate());
        }
        if (updateDto.getStatus() != null) {
            progress.setStatus(updateDto.getStatus());
        }

        CompetenceProgress savedProgress = competenceProgressRepository.save(progress);
        return convertCompetenceProgressToDto(savedProgress);
    }

    public CompetenceProgressDto approveCompetence(Integer progressId, Integer educatorId, ApproveCompetenceDto approveDto) {
        CompetenceProgress progress = competenceProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Progreso de competencia no encontrado con ID: " + progressId));

        if (progress.getStatus() != CompetenceStatus.COMPLETED) {
            throw new RuntimeException("Solo se pueden aprobar competencias que estén completadas");
        }

        progress.setStatus(CompetenceStatus.APPROVED);
        progress.setApprovalDate(LocalDate.now());
        progress.setEducatorComments(approveDto.getEducatorComments());

        CompetenceProgress savedProgress = competenceProgressRepository.save(progress);
        return convertCompetenceProgressToDto(savedProgress);
    }

    @Transactional(readOnly = true)
    public List<CompetenceProgressDto> getPendingApprovals() {
        return competenceProgressRepository.findPendingApprovals()
                .stream()
                .map(this::convertCompetenceProgressToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompetenceProgressDto> getPendingApprovalsByEducator(Integer educatorId) {
        return competenceProgressRepository.findPendingApprovalsByEducator(educatorId)
                .stream()
                .map(this::convertCompetenceProgressToDto)
                .collect(Collectors.toList());
    }

    public void removeCompetenceFromMarchSheet(Integer progressId) {
        if (!competenceProgressRepository.existsById(progressId)) {
            throw new RuntimeException("Progreso de competencia no encontrado con ID: " + progressId);
        }
        competenceProgressRepository.deleteById(progressId);
    }

    private ProgressionStatsDto calculateProgressionStats(List<CompetenceProgress> progressList) {
        int total = progressList.size();
        int completed = (int) progressList.stream()
                .filter(p -> p.getStatus() == CompetenceStatus.COMPLETED || p.getStatus() == CompetenceStatus.APPROVED)
                .count();
        int inProgress = (int) progressList.stream()
                .filter(p -> p.getStatus() == CompetenceStatus.IN_PROGRESS)
                .count();
        int pending = (int) progressList.stream()
                .filter(p -> p.getStatus() == CompetenceStatus.PENDING)
                .count();

        double generalPercentage = total > 0 ? (double) completed / total * 100 : 0.0;

        Map<GrowthArea, ProgressionStatsDto.AreaStatsDto> byArea = new HashMap<>();
        for (GrowthArea area : GrowthArea.values()) {
            List<CompetenceProgress> areaProgress = progressList.stream()
                    .filter(p -> p.getCompetence().getGrowthArea() == area)
                    .collect(Collectors.toList());

            int areaTotal = areaProgress.size();
            int areaCompleted = (int) areaProgress.stream()
                    .filter(p -> p.getStatus() == CompetenceStatus.COMPLETED || p.getStatus() == CompetenceStatus.APPROVED)
                    .count();
            double areaPercentage = areaTotal > 0 ? (double) areaCompleted / areaTotal * 100 : 0.0;

            byArea.put(area, ProgressionStatsDto.AreaStatsDto.builder()
                    .total(areaTotal)
                    .completed(areaCompleted)
                    .percentage(areaPercentage)
                    .build());
        }

        return ProgressionStatsDto.builder()
                .totalCompetences(total)
                .completed(completed)
                .inProgress(inProgress)
                .pending(pending)
                .generalPercentage(generalPercentage)
                .byArea(byArea)
                .build();
    }

    private MarchSheetDto convertMarchSheetToDto(MarchSheet marchSheet) {
        List<CompetenceProgressDto> progressDtos = new ArrayList<>();
        if (marchSheet.getCompetenceProgress() != null) {
            progressDtos = marchSheet.getCompetenceProgress()
                    .stream()
                    .map(this::convertCompetenceProgressToDto)
                    .collect(Collectors.toList());
        }


        return MarchSheetDto.builder()
                .id(marchSheet.getId())
                .memberId(marchSheet.getMember().getId())
                .memberName(marchSheet.getMember().getName() + " " + marchSheet.getMember().getLastname())
                .totem(marchSheet.getTotem())
                .progressionStage(marchSheet.getProgressionStage())
                .createdAt(marchSheet.getCreatedAt())
                .updatedAt(marchSheet.getUpdatedAt())
                .competenceProgress(progressDtos)
                .build();
    }

    private CompetenceProgressDto convertCompetenceProgressToDto(CompetenceProgress progress) {
        CompetenceDto competenceDto = competenceService.getCompetenceById(progress.getCompetence().getId())
                .orElse(null);

        return CompetenceProgressDto.builder()
                .id(progress.getId())
                .competenceId(progress.getCompetence().getId())
                .competence(competenceDto)
                .marchSheetId(progress.getMarchSheet().getId())
                .ownAction(progress.getOwnAction())
                .achievement(progress.getAchievement())
                .startDate(progress.getStartDate())
                .completionDate(progress.getCompletionDate())
                .status(progress.getStatus())
                .approvedByEducatorId(progress.getApprovedByEducator() != null ? progress.getApprovedByEducator().getId() : null)
                .approvedByEducatorName(progress.getApprovedByEducator() != null ? 
                    progress.getApprovedByEducator().getName() + " " + progress.getApprovedByEducator().getLastname() : null)
                .approvalDate(progress.getApprovalDate())
                .educatorComments(progress.getEducatorComments())
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}