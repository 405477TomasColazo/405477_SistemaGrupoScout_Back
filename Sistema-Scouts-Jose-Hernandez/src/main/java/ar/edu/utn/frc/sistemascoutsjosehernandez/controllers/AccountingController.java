package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.AccountType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.accounting.AccountingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounting", description = "Accounting management endpoints")
public class AccountingController {

    private final AccountingService accountingService;

    // Account endpoints
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts", description = "Retrieve all active accounts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts")
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        try {
            List<AccountDto> accounts = accountingService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Error retrieving accounts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/accounts/by-type")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get accounts by type", description = "Retrieve accounts filtered by account type")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts")
    public ResponseEntity<List<AccountDto>> getAccountsByType(
            @Parameter(description = "Account type") @RequestParam AccountType type) {
        try {
            List<AccountDto> accounts = accountingService.getAccountsByType(type);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Error retrieving accounts by type {}: {}", type, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/accounts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get account by ID", description = "Retrieve a specific account by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved account")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id) {
        try {
            AccountDto account = accountingService.getAccountById(id);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            log.error("Account not found with id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving account {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Accounting entry endpoints
    @GetMapping("/entries")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get accounting entries", description = "Retrieve accounting entries with filtering and pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounting entries")
    public ResponseEntity<Page<AccountingEntryDto>> getAccountingEntries(
            @Parameter(description = "Start date filter") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "End date filter") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Entry status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Origin type filter") @RequestParam(required = false) String originType,
            @Parameter(description = "Description filter") @RequestParam(required = false) String description,
            @Parameter(description = "Account ID filter") @RequestParam(required = false) Long accountId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            AccountingFiltersDto filters = AccountingFiltersDto.builder()
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .status(status != null ? ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.EntryStatus.valueOf(status.toUpperCase()) : null)
                    .originType(originType != null ? ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.OriginType.valueOf(originType.toUpperCase()) : null)
                    .description(description)
                    .accountId(accountId)
                    .build();

            Page<AccountingEntryDto> entries = accountingService.getAccountingEntries(filters, page, size);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            log.error("Error retrieving accounting entries: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/entries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get accounting entry by ID", description = "Retrieve a specific accounting entry with movements")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounting entry")
    @ApiResponse(responseCode = "404", description = "Accounting entry not found")
    public ResponseEntity<AccountingEntryDto> getAccountingEntryById(@PathVariable Long id) {
        try {
            AccountingEntryDto entry = accountingService.getAccountingEntryById(id);
            return ResponseEntity.ok(entry);
        } catch (RuntimeException e) {
            log.error("Accounting entry not found with id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving accounting entry {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/entries")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create manual accounting entry", description = "Create a new manual accounting entry")
    @ApiResponse(responseCode = "201", description = "Successfully created accounting entry")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<AccountingEntryDto> createAccountingEntry(
            @Valid @RequestBody CreateAccountingEntryDto createDto) {
        try {
            AccountingEntryDto createdEntry = accountingService.createManualAccountingEntry(createDto);
            log.info("Created manual accounting entry: {}", createdEntry.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEntry);
        } catch (RuntimeException e) {
            log.error("Error creating accounting entry: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating accounting entry: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/entries/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel accounting entry", description = "Cancel a manual accounting entry and create reversal")
    @ApiResponse(responseCode = "200", description = "Successfully cancelled accounting entry")
    @ApiResponse(responseCode = "400", description = "Invalid operation")
    @ApiResponse(responseCode = "404", description = "Accounting entry not found")
    public ResponseEntity<AccountingEntryDto> cancelAccountingEntry(
            @PathVariable Long id,
            @Parameter(description = "Cancellation reason") @RequestParam String reason) {
        try {
            AccountingEntryDto cancelledEntry = accountingService.cancelAccountingEntry(id, reason);
            log.info("Cancelled accounting entry {}: {}", id, reason);
            return ResponseEntity.ok(cancelledEntry);
        } catch (RuntimeException e) {
            log.error("Error cancelling accounting entry {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error cancelling accounting entry {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Report endpoints
    @GetMapping("/reports/journal")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate journal report", description = "Generate a journal (libro diario) report for a date range")
    @ApiResponse(responseCode = "200", description = "Successfully generated journal report")
    public ResponseEntity<AccountingReportDto> generateJournalReport(
            @Parameter(description = "Start date", required = true) @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date", required = true) @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AccountingReportDto report = accountingService.generateJournalReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error generating journal report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reports/balance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate balance report", description = "Generate a balance report for a date range")
    @ApiResponse(responseCode = "200", description = "Successfully generated balance report")
    public ResponseEntity<AccountingReportDto> generateBalanceReport(
            @Parameter(description = "Start date", required = true) @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date", required = true) @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AccountingReportDto report = accountingService.generateBalanceReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error generating balance report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Dashboard/Statistics endpoints
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get accounting dashboard statistics", description = "Get summary statistics for the accounting dashboard")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounting statistics")
    public ResponseEntity<AccountingDashboardStatsDto> getDashboardStats(
            @Parameter(description = "Start date for stats") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "End date for stats") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        try {
            // Default to current month if no dates provided
            LocalDate startDate = dateFrom != null ? dateFrom : LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = dateTo != null ? dateTo : LocalDate.now();
            
            log.debug("Getting dashboard stats for period: {} to {}", startDate, endDate);

            // Initialize stats with default values
            AccountingDashboardStatsDto stats = AccountingDashboardStatsDto.builder()
                    .periodStart(startDate)
                    .periodEnd(endDate)
                    .totalEntries(0)
                    .totalDebits(BigDecimal.ZERO)
                    .totalCredits(BigDecimal.ZERO)
                    .balanceDifference(BigDecimal.ZERO)
                    .accountsWithMovements(0)
                    .build();

            try {
                // Try to generate reports, but don't fail if no data exists
                AccountingReportDto journalReport = accountingService.generateJournalReport(startDate, endDate);
                if (journalReport != null) {
                    stats = AccountingDashboardStatsDto.builder()
                            .periodStart(stats.getPeriodStart())
                            .periodEnd(stats.getPeriodEnd())
                            .totalEntries(journalReport.getEntries() != null ? journalReport.getEntries().size() : 0)
                            .totalDebits(journalReport.getTotalDebits() != null ? journalReport.getTotalDebits() : BigDecimal.ZERO)
                            .totalCredits(journalReport.getTotalCredits() != null ? journalReport.getTotalCredits() : BigDecimal.ZERO)
                            .balanceDifference(journalReport.getBalanceDifference() != null ? journalReport.getBalanceDifference() : BigDecimal.ZERO)
                            .accountsWithMovements(stats.getAccountsWithMovements())
                            .automaticEntries(stats.getAutomaticEntries())
                            .manualEntries(stats.getManualEntries())
                            .build();
                }
            } catch (Exception e) {
                log.warn("Could not generate journal report for dashboard: {}", e.getMessage());
            }

            try {
                AccountingReportDto balanceReport = accountingService.generateBalanceReport(startDate, endDate);
                if (balanceReport != null && balanceReport.getAccountBalances() != null) {
                    stats = AccountingDashboardStatsDto.builder()
                            .periodStart(stats.getPeriodStart())
                            .periodEnd(stats.getPeriodEnd())
                            .totalEntries(stats.getTotalEntries())
                            .totalDebits(stats.getTotalDebits())
                            .totalCredits(stats.getTotalCredits())
                            .balanceDifference(stats.getBalanceDifference())
                            .accountsWithMovements(balanceReport.getAccountBalances().size())
                            .automaticEntries(stats.getAutomaticEntries())
                            .manualEntries(stats.getManualEntries())
                            .build();
                }
            } catch (Exception e) {
                log.warn("Could not generate balance report for dashboard: {}", e.getMessage());
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving dashboard stats: ", e);
            // Return empty stats instead of error
            AccountingDashboardStatsDto emptyStats = AccountingDashboardStatsDto.builder()
                    .periodStart(dateFrom != null ? dateFrom : LocalDate.now().withDayOfMonth(1))
                    .periodEnd(dateTo != null ? dateTo : LocalDate.now())
                    .totalEntries(0)
                    .totalDebits(BigDecimal.ZERO)
                    .totalCredits(BigDecimal.ZERO)
                    .balanceDifference(BigDecimal.ZERO)
                    .accountsWithMovements(0)
                    .build();
            return ResponseEntity.ok(emptyStats);
        }
    }
}