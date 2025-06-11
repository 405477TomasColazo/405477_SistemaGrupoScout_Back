package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.progression.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.progression.ProgressionService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/progression")
@RequiredArgsConstructor
@Tag(name = "Progression", description = "Scout progression management endpoints")
public class ProgressionController {

    private final ProgressionService progressionService;
    private final SecurityUtils securityUtils;

    @GetMapping("/march-sheet/member/{memberId}")
    @Operation(summary = "Get march sheet by member ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR') or @securityUtils.isOwnerOrFamily(authentication, #memberId)")
    public ResponseEntity<MarchSheetDto> getMarchSheetByMember(@PathVariable Integer memberId) {
        return progressionService.getMarchSheetByMember(memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/competence-progress/member/{memberId}")
    @Operation(summary = "Get competence progress by member ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR') or @securityUtils.isOwnerOrFamily(authentication, #memberId)")
    public ResponseEntity<List<CompetenceProgressDto>> getCompetenceProgressByMember(@PathVariable Integer memberId) {
        List<CompetenceProgressDto> progress = progressionService.getCompetenceProgressByMember(memberId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/stats/member/{memberId}")
    @Operation(summary = "Get progression statistics by member ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR') or @securityUtils.isOwnerOrFamily(authentication, #memberId)")
    public ResponseEntity<ProgressionStatsDto> getProgressionStats(@PathVariable Integer memberId) {
        ProgressionStatsDto stats = progressionService.getProgressionStats(memberId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/march-sheet")
    @Operation(summary = "Create new march sheet")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR') or @securityUtils.isOwnerOrFamily(authentication, #createDto.memberId)")
    public ResponseEntity<MarchSheetDto> createMarchSheet(@Valid @RequestBody CreateMarchSheetDto createDto) {
        try {
            MarchSheetDto createdMarchSheet = progressionService.createMarchSheet(createDto);
            return ResponseEntity.ok(createdMarchSheet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/march-sheet/{marchSheetId}")
    @Operation(summary = "Update march sheet")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR') or @securityUtils.isMarchSheetOwnerOrFamily(authentication, #marchSheetId)")
    public ResponseEntity<MarchSheetDto> updateMarchSheet(
            @PathVariable Integer marchSheetId,
            @Valid @RequestBody CreateMarchSheetDto updateDto) {
        try {
            MarchSheetDto updatedMarchSheet = progressionService.updateMarchSheet(marchSheetId, updateDto);
            return ResponseEntity.ok(updatedMarchSheet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/march-sheet/{marchSheetId}/competence/{competenceId}")
    @Operation(summary = "Add competence to march sheet")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR') or @securityUtils.isMarchSheetOwnerOrFamily(authentication, #marchSheetId)")
    public ResponseEntity<CompetenceProgressDto> addCompetenceToMarchSheet(
            @PathVariable Integer marchSheetId,
            @PathVariable Integer competenceId) {
        try {
            CompetenceProgressDto progress = progressionService.addCompetenceToMarchSheet(marchSheetId, competenceId);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/competence-progress/{progressId}")
    @Operation(summary = "Update competence progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR') or @securityUtils.isCompetenceProgressOwnerOrFamily(authentication, #progressId)")
    public ResponseEntity<CompetenceProgressDto> updateCompetenceProgress(
            @PathVariable Integer progressId,
            @RequestBody UpdateCompetenceProgressDto updateDto) {
        try {
            CompetenceProgressDto updatedProgress = progressionService.updateCompetenceProgress(progressId, updateDto);
            return ResponseEntity.ok(updatedProgress);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/competence-progress/{progressId}/approve")
    @Operation(summary = "Approve competence (Educators and Admins only)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR')")
    public ResponseEntity<CompetenceProgressDto> approveCompetence(
            @PathVariable Integer progressId,
            @RequestBody ApproveCompetenceDto approveDto) {
        try {
            Integer educatorId = securityUtils.getCurrentUserId();
            CompetenceProgressDto approvedProgress = progressionService.approveCompetence(progressId, educatorId, approveDto);
            return ResponseEntity.ok(approvedProgress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/competence-progress/{progressId}")
    @Operation(summary = "Remove competence from march sheet")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR') or @securityUtils.isCompetenceProgressOwnerOrFamily(authentication, #progressId)")
    public ResponseEntity<Void> removeCompetenceFromMarchSheet(@PathVariable Integer progressId) {
        try {
            progressionService.removeCompetenceFromMarchSheet(progressId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get all pending approvals (Educators and Admins only)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR')")
    public ResponseEntity<List<CompetenceProgressDto>> getPendingApprovals() {
        List<CompetenceProgressDto> pendingApprovals;
        
        if (securityUtils.hasRole("ADMIN")) {
            pendingApprovals = progressionService.getPendingApprovals();
        } else {
            Integer educatorId = securityUtils.getCurrentUserId();
            pendingApprovals = progressionService.getPendingApprovalsByEducator(educatorId);
        }
        
        return ResponseEntity.ok(pendingApprovals);
    }

    @PutMapping("/progression-stage")
    @Operation(summary = "Update progression stage (Educators and Admins only)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDUCATOR')")
    public ResponseEntity<MarchSheetDto> updateProgressionStage(@Valid @RequestBody UpdateProgressionStageDto updateDto) {
        try {
            MarchSheetDto updatedMarchSheet = progressionService.updateProgressionStage(updateDto);
            return ResponseEntity.ok(updatedMarchSheet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}