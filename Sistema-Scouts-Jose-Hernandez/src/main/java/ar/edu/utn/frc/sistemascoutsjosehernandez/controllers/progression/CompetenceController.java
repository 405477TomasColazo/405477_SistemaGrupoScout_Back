package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression.CompetenceDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.GrowthArea;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.progression.CompetenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competences")
@RequiredArgsConstructor
@Tag(name = "Competences", description = "Competences management endpoints")
public class CompetenceController {

    private final CompetenceService competenceService;

    @GetMapping
    @Operation(summary = "Get all competences")
    public ResponseEntity<List<CompetenceDto>> getAllCompetences(
            @RequestParam(required = false) GrowthArea area,
            @RequestParam(required = false) Integer sectionId) {
        
        List<CompetenceDto> competences;
        
        if (area != null && sectionId != null) {
            competences = competenceService.getCompetencesBySectionAndArea(sectionId, area);
        } else if (area != null) {
            competences = competenceService.getCompetencesByArea(area);
        } else if (sectionId != null) {
            competences = competenceService.getCompetencesBySectionAndArea(sectionId, null);
        } else {
            competences = competenceService.getAllCompetences();
        }
        
        return ResponseEntity.ok(competences);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get competence by ID")
    public ResponseEntity<CompetenceDto> getCompetenceById(@PathVariable Integer id) {
        return competenceService.getCompetenceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/general")
    @Operation(summary = "Get general competences (not section-specific)")
    public ResponseEntity<List<CompetenceDto>> getGeneralCompetences() {
        List<CompetenceDto> competences = competenceService.getGeneralCompetences();
        return ResponseEntity.ok(competences);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new competence (Admin only)")
    public ResponseEntity<CompetenceDto> createCompetence(@RequestBody CompetenceDto competenceDto) {
        CompetenceDto createdCompetence = competenceService.createCompetence(competenceDto);
        return ResponseEntity.ok(createdCompetence);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update competence (Admin only)")
    public ResponseEntity<CompetenceDto> updateCompetence(
            @PathVariable Integer id,
            @RequestBody CompetenceDto competenceDto) {
        try {
            CompetenceDto updatedCompetence = competenceService.updateCompetence(id, competenceDto);
            return ResponseEntity.ok(updatedCompetence);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete competence (Admin only)")
    public ResponseEntity<Void> deleteCompetence(@PathVariable Integer id) {
        try {
            competenceService.deleteCompetence(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}