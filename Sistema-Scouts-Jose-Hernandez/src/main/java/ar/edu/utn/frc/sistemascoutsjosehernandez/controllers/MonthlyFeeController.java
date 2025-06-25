package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.FeeGenerationLog;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.MemberType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.GlobalFeeConfiguration;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FeeGenerationLogRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.UserRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.SectionRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberTypeRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.MonthlyFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/monthly-fees")
@RequiredArgsConstructor
@Tag(name = "Monthly Fee Management", description = "Admin endpoints for managing monthly fees")
public class MonthlyFeeController {

    private final MonthlyFeeService monthlyFeeService;
    private final FeeGenerationLogRepository feeGenerationLogRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final MemberTypeRepository memberTypeRepository;

    @PostMapping("/generate/{month}")
    @Operation(summary = "Generate monthly fees for a specific month")
    public ResponseEntity<?> generateMonthlyFees(@PathVariable String month) {
        try {
            User currentUser = getCurrentUser();
            MonthlyFeeService.FeeGenerationResult result = monthlyFeeService.generateMonthlyFeesManually(month, currentUser);
            
            if (result.hasError()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", result.getError(),
                    "membersProcessed", result.getMembersProcessed(),
                    "feesGenerated", result.getFeesGenerated(),
                    "feesUpdated", result.getFeesUpdated()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Monthly fees generated successfully",
                "membersProcessed", result.getMembersProcessed(),
                "feesGenerated", result.getFeesGenerated(),
                "feesUpdated", result.getFeesUpdated(),
                "month", month
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate/current")
    @Operation(summary = "Generate monthly fees for current month")
    public ResponseEntity<?> generateCurrentMonthFees() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return generateMonthlyFees(currentMonth);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate monthly fees with request body")
    public ResponseEntity<?> generateMonthlyFeesWithRequest(@RequestBody FeeGenerationRequest request) {
        try {
            User currentUser = getCurrentUser();
            MonthlyFeeService.FeeGenerationResult result = monthlyFeeService.generateMonthlyFeesManually(request.getTargetMonth(), currentUser);
            
            if (result.hasError()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", result.getError(),
                    "membersProcessed", result.getMembersProcessed(),
                    "feesGenerated", result.getFeesGenerated(),
                    "feesUpdated", result.getFeesUpdated()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Monthly fees generated successfully",
                "totalGenerated", result.getFeesGenerated() + result.getFeesUpdated(),
                "month", request.getTargetMonth()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/global-price")
    @Operation(summary = "Get current global monthly fee price")
    public ResponseEntity<Map<String, Object>> getGlobalFeePrice() {
        BigDecimal currentPrice = monthlyFeeService.getCurrentGlobalFeePrice();
        return ResponseEntity.ok(Map.of(
            "monthlyFeeAmount", currentPrice,
            "message", "Current global monthly fee price"
        ));
    }

    @PutMapping("/global-price")
    @Operation(summary = "Update global monthly fee price")
    public ResponseEntity<?> updateGlobalFeePrice(@RequestBody GlobalPriceUpdateRequest request) {
        try {
            User currentUser = getCurrentUser();
            MonthlyFeeService.GlobalPriceUpdateResult result = monthlyFeeService.updateGlobalFeePrice(
                request.getNewAmount(),
                currentUser
            );
            
            return ResponseEntity.ok(Map.of(
                "message", result.getMessage(),
                "newPrice", result.getNewPrice(),
                "updatedPendingFees", result.getUpdatedPendingFees()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/generation-logs")
    @Operation(summary = "Get fee generation logs with pagination")
    public ResponseEntity<Page<FeeGenerationLog>> getGenerationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<FeeGenerationLog> logs = feeGenerationLogRepository.findAllByOrderByExecutionDateDesc(
            PageRequest.of(page, size)
        );
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs")
    @Operation(summary = "Get fee generation logs with filtering")
    public ResponseEntity<Map<String, Object>> getGenerationLogsWithFilters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String generationType) {
        
        Page<FeeGenerationLog> logsPage;
        
        if (generationType != null && !generationType.isEmpty()) {
            logsPage = feeGenerationLogRepository.findWithFilters(
                generationType,
                PageRequest.of(page, limit)
            );
        } else {
            logsPage = feeGenerationLogRepository.findAllByOrderByExecutionDateDesc(
                PageRequest.of(page, limit)
            );
        }
        
        return ResponseEntity.ok(Map.of(
            "logs", logsPage.getContent(),
            "total", logsPage.getTotalElements()
        ));
    }

    @GetMapping("/generation-logs/{month}")
    @Operation(summary = "Get generation logs for a specific month")
    public ResponseEntity<List<FeeGenerationLog>> getGenerationLogsForMonth(@PathVariable String month) {
        List<FeeGenerationLog> logs = monthlyFeeService.getGenerationLogForMonth(month);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/status/{month}")
    @Operation(summary = "Check if fees have been generated for a specific month")
    public ResponseEntity<Map<String, Object>> getFeeGenerationStatus(@PathVariable String month) {
        boolean hasFeesGenerated = monthlyFeeService.hasFeesGeneratedForMonth(month);
        List<FeeGenerationLog> logs = monthlyFeeService.getGenerationLogForMonth(month);
        
        return ResponseEntity.ok(Map.of(
            "month", month,
            "hasFeesGenerated", hasFeesGenerated,
            "generationCount", logs.size(),
            "lastGeneration", logs.isEmpty() ? null : logs.get(0)
        ));
    }

    @GetMapping("/status/current")
    @Operation(summary = "Check current month fee generation status")
    public ResponseEntity<Map<String, Object>> getCurrentMonthStatus() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return getFeeGenerationStatus(currentMonth);
    }

    @GetMapping("/sections")
    @Operation(summary = "Get all sections")
    public ResponseEntity<List<Section>> getAllSections() {
        List<Section> sections = sectionRepository.findAll();
        return ResponseEntity.ok(sections);
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // DTOs
    public static class GlobalPriceUpdateRequest {
        private BigDecimal newAmount;

        // Getters and setters
        public BigDecimal getNewAmount() { return newAmount; }
        public void setNewAmount(BigDecimal newAmount) { this.newAmount = newAmount; }
    }

    public static class FeeGenerationRequest {
        private String targetMonth;
        private String section;

        // Getters and setters
        public String getTargetMonth() { return targetMonth; }
        public void setTargetMonth(String targetMonth) { this.targetMonth = targetMonth; }
        
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
    }
}