package ar.edu.utn.frc.sistemascoutsjosehernandez.services.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression.CompetenceDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression.SuggestedActionDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.Competence;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.GrowthArea;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.SuggestedAction;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.CompetenceRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.SuggestedActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetenceService {

    private final CompetenceRepository competenceRepository;
    private final SuggestedActionRepository suggestedActionRepository;

    @Transactional(readOnly = true)
    public List<CompetenceDto> getAllCompetences() {
        return competenceRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompetenceDto> getCompetencesByArea(GrowthArea growthArea) {
        return competenceRepository.findByGrowthArea(growthArea)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompetenceDto> getCompetencesBySectionAndArea(Integer sectionId, GrowthArea growthArea) {
        return competenceRepository.findBySectionAndGrowthArea(sectionId, growthArea)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompetenceDto> getGeneralCompetences() {
        return competenceRepository.findBySectionIsNull()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CompetenceDto> getCompetenceById(Integer id) {
        return competenceRepository.findById(id)
                .map(this::convertToDto);
    }

    public CompetenceDto createCompetence(CompetenceDto competenceDto) {
        Competence competence = convertToEntity(competenceDto);
        Competence savedCompetence = competenceRepository.save(competence);
        return convertToDto(savedCompetence);
    }

    public CompetenceDto updateCompetence(Integer id, CompetenceDto competenceDto) {
        return competenceRepository.findById(id)
                .map(existingCompetence -> {
                    updateCompetenceFromDto(existingCompetence, competenceDto);
                    Competence savedCompetence = competenceRepository.save(existingCompetence);
                    return convertToDto(savedCompetence);
                })
                .orElseThrow(() -> new RuntimeException("Competencia no encontrada con ID: " + id));
    }

    public void deleteCompetence(Integer id) {
        if (!competenceRepository.existsById(id)) {
            throw new RuntimeException("Competencia no encontrada con ID: " + id);
        }
        competenceRepository.deleteById(id);
    }

    private CompetenceDto convertToDto(Competence competence) {
        List<SuggestedActionDto> suggestedActionDtos = competence.getSuggestedActions()
                .stream()
                .map(this::convertSuggestedActionToDto)
                .collect(Collectors.toList());

        return CompetenceDto.builder()
                .id(competence.getId())
                .title(competence.getTitle())
                .description(competence.getDescription())
                .growthArea(competence.getGrowthArea())
                .sectionId(competence.getSection() != null ? competence.getSection().getId() : null)
                .sectionName(competence.getSection() != null ? competence.getSection().getDescription() : null)
                .suggestedActions(suggestedActionDtos)
                .guidingQuestions(competence.getGuidingQuestions())
                .build();
    }

    private SuggestedActionDto convertSuggestedActionToDto(SuggestedAction suggestedAction) {
        return SuggestedActionDto.builder()
                .id(suggestedAction.getId())
                .description(suggestedAction.getDescription())
                .competenceId(suggestedAction.getCompetence().getId())
                .build();
    }

    private Competence convertToEntity(CompetenceDto dto) {
        return Competence.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .growthArea(dto.getGrowthArea())
                .guidingQuestions(dto.getGuidingQuestions())
                .build();
    }

    private void updateCompetenceFromDto(Competence competence, CompetenceDto dto) {
        competence.setTitle(dto.getTitle());
        competence.setDescription(dto.getDescription());
        competence.setGrowthArea(dto.getGrowthArea());
        competence.setGuidingQuestions(dto.getGuidingQuestions());
    }
}