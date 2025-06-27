package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard.DashboardStatsDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard.EducatorDashboardDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.dashboard.FamilyDashboardDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.DashboardService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics and data endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @Operation(summary = "Get admin dashboard statistics", 
               description = "Returns comprehensive dashboard statistics for administrators")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DashboardStatsDto> getAdminDashboard() {
        try {
            DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/educator")
    @Operation(summary = "Get educator dashboard statistics", 
               description = "Returns dashboard statistics for the authenticated educator")
    @PreAuthorize("hasAuthority('EDUCATOR')")
    public ResponseEntity<EducatorDashboardDto> getEducatorDashboard() {
        try {
            Integer userId = SecurityUtils.getCurrentUserId();
            EducatorDashboardDto stats = dashboardService.getEducatorDashboardStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/educator/{educatorId}")
    @Operation(summary = "Get educator dashboard statistics by ID", 
               description = "Returns dashboard statistics for a specific educator (admin only)")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<EducatorDashboardDto> getEducatorDashboardById(@PathVariable Integer educatorId) {
        try {
            EducatorDashboardDto stats = dashboardService.getEducatorDashboardStats(educatorId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/family")
    @Operation(summary = "Get family dashboard statistics", 
               description = "Returns dashboard statistics for the authenticated family")
    @PreAuthorize("hasAuthority('FAMILY')")
    public ResponseEntity<FamilyDashboardDto> getFamilyDashboard() {
        try {
            Integer userId = SecurityUtils.getCurrentUserId();
            FamilyDashboardDto stats = dashboardService.getFamilyDashboardStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/family/{familyUserId}")
    @Operation(summary = "Get family dashboard statistics by ID", 
               description = "Returns dashboard statistics for a specific family (admin only)")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FamilyDashboardDto> getFamilyDashboardById(@PathVariable Integer familyUserId) {
        try {
            FamilyDashboardDto stats = dashboardService.getFamilyDashboardStats(familyUserId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}