package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyFeeService {

    private final FeeRepository feeRepository;
    private final FeeGenerationLogRepository feeGenerationLogRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final MemberTypeRepository memberTypeRepository;
    private final GlobalFeeConfigurationRepository globalFeeConfigurationRepository;

    /**
     * Generates monthly fees for all active protagonists
     * Called automatically on the 1st of each month at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 1 * ?") // Execute on 1st day of month at 9:00 AM
    @Transactional
    public void generateMonthlyFeesAutomatically() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("Starting automatic monthly fee generation for month: {}", currentMonth);
        
        try {
            FeeGenerationResult result = generateMonthlyFees(currentMonth, null, "AUTOMATIC");
            log.info("Automatic fee generation completed successfully. Generated: {}, Updated: {}", 
                    result.getFeesGenerated(), result.getFeesUpdated());
        } catch (Exception e) {
            log.error("Error during automatic monthly fee generation", e);
        }
    }

    /**
     * Manually generates monthly fees for a specific month
     */
    @Transactional
    public FeeGenerationResult generateMonthlyFeesManually(String month, User executedByUser) {
        log.info("Starting manual monthly fee generation for month: {} by user: {}", month, executedByUser.getEmail());
        return generateMonthlyFees(month, executedByUser, "MANUAL");
    }

    /**
     * Core method to generate monthly fees
     */
    private FeeGenerationResult generateMonthlyFees(String month, User executedByUser, String executionType) {
        FeeGenerationResult result = new FeeGenerationResult();
        StringBuilder executionDetails = new StringBuilder();
        List<String> errors = new ArrayList<>();
        
        try {
            // Get all active protagonists (only protagonists pay monthly fees)
            List<Member> protagonists = memberRepository.findActiveProtagonistMembers();
            result.setMembersProcessed(protagonists.size());
            
            executionDetails.append(String.format("Processing %d active protagonists for month %s\n", protagonists.size(), month));
            
            for (Member member : protagonists) {
                try {
                    boolean feeExists = processMemberMonthlyFee(member, month);
                    if (feeExists) {
                        result.incrementFeesUpdated();
                    } else {
                        result.incrementFeesGenerated();
                    }
                } catch (Exception e) {
                    log.error("Error processing member {}: {}", member.getId(), e.getMessage());
                    errors.add(String.format("Member %s %s: %s", member.getName(), member.getLastname(), e.getMessage()));
                }
            }
            
            // Add summary instead of individual lines
            executionDetails.append(String.format("Generated: %d new fees, Updated: %d existing fees\n", 
                result.getFeesGenerated(), result.getFeesUpdated()));
            
            if (errors.isEmpty()) {
                executionDetails.append("Completed successfully with 0 errors");
            } else {
                executionDetails.append(String.format("Completed with %d errors:\n", errors.size()));
                // Only add first few errors to avoid field length issues
                int maxErrors = Math.min(errors.size(), 5);
                for (int i = 0; i < maxErrors; i++) {
                    executionDetails.append("- ").append(errors.get(i)).append("\n");
                }
                if (errors.size() > 5) {
                    executionDetails.append(String.format("... and %d more errors", errors.size() - 5));
                }
            }
            
            // Save generation log
            FeeGenerationLog logEntry = FeeGenerationLog.builder()
                    .generationMonth(month)
                    .executionDate(LocalDateTime.now())
                    .totalMembersProcessed(result.getMembersProcessed())
                    .totalFeesGenerated(result.getFeesGenerated())
                    .totalFeesUpdated(result.getFeesUpdated())
                    .executionType(executionType)
                    .executedByUser(executedByUser)
                    .executionDetails(executionDetails.toString())
                    .status(FeeGenerationLog.FeeGenerationStatus.SUCCESS)
                    .build();
            
            feeGenerationLogRepository.save(logEntry);
            
        } catch (Exception e) {
            log.error("Error during fee generation", e);
            result.setError(e.getMessage());
            
            // Save error log with concise details
            String errorDetails = String.format("Processing %d protagonists for month %s\nFATAL ERROR: %s", 
                result.getMembersProcessed(), month, e.getMessage());
            
            FeeGenerationLog errorLog = FeeGenerationLog.builder()
                    .generationMonth(month)
                    .executionDate(LocalDateTime.now())
                    .totalMembersProcessed(result.getMembersProcessed())
                    .totalFeesGenerated(result.getFeesGenerated())
                    .totalFeesUpdated(result.getFeesUpdated())
                    .executionType(executionType)
                    .executedByUser(executedByUser)
                    .executionDetails(errorDetails)
                    .status(FeeGenerationLog.FeeGenerationStatus.FAILED)
                    .build();
            
            feeGenerationLogRepository.save(errorLog);
        }
        
        return result;
    }

    /**
     * Processes monthly fee for a single member
     * Returns true if fee already existed (updated), false if new fee created
     */
    private boolean processMemberMonthlyFee(Member member, String month) {
        // Check if fee already exists for this member and month
        Optional<Fee> existingFee = feeRepository.findByMemberAndPeriod(member.getId(), month);
        
        // Get global fee amount
        Optional<GlobalFeeConfiguration> globalConfig = globalFeeConfigurationRepository.findActiveConfiguration();
        
        if (globalConfig.isEmpty()) {
            log.warn("No global fee configuration found. Please set up the monthly fee amount.");
            return false;
        }
        
        BigDecimal feeAmount = globalConfig.get().getMonthlyFeeAmount();
        
        if (existingFee.isPresent()) {
            // Update existing fee amount if it's still pending
            Fee fee = existingFee.get();
            if (fee.getStatus() == PaymentStatus.PENDING) {
                fee.setAmount(feeAmount);
                feeRepository.save(fee);
                log.debug("Updated fee amount for member {} month {} to {}", member.getId(), month, feeAmount);
            }
            return true;
        } else {
            // Create new fee
            Fee newFee = Fee.builder()
                    .member(member)
                    .description("Cuota mensual " + DateUtils.convertDate(month))
                    .amount(feeAmount)
                    .period(month)
                    .status(PaymentStatus.PENDING)
                    .build();
            
            feeRepository.save(newFee);
            log.debug("Created new fee for member {} month {} amount {}", member.getId(), month, feeAmount);
            return false;
        }
    }

    /**
     * Updates the global monthly fee amount and adjusts all pending fees
     */
    @Transactional
    public GlobalPriceUpdateResult updateGlobalFeePrice(BigDecimal newAmount, User updatedByUser) {
        
        // First, deactivate any existing configuration
        Optional<GlobalFeeConfiguration> existingConfig = globalFeeConfigurationRepository.findActiveConfiguration();
        if (existingConfig.isPresent()) {
            existingConfig.get().setIsActive(false);
            globalFeeConfigurationRepository.save(existingConfig.get());
        }
        
        // Create new active configuration
        GlobalFeeConfiguration newConfig = GlobalFeeConfiguration.builder()
                .monthlyFeeAmount(newAmount)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        globalFeeConfigurationRepository.save(newConfig);
        
        // Update all pending protagonist fees to the new amount
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int updatedFees = feeRepository.updateAllPendingProtagonistFeesAmount(currentMonth, newAmount);
        
        log.info("Updated global fee price to {} and adjusted {} pending fees", newAmount, updatedFees);
        
        // Send email notifications to affected families
        sendFeeUpdateNotifications(newAmount);

        // Create generation log entry
        FeeGenerationLog logEntry = FeeGenerationLog.builder()
                .executionType("GLOBAL_PRICE_UPDATE")
                .executionDate(LocalDateTime.now())
                .generationMonth(currentMonth)
                .totalMembersProcessed(0)
                .totalFeesGenerated(0)
                .totalFeesUpdated(updatedFees)
                .executedByUser(updatedByUser)
                .status(FeeGenerationLog.FeeGenerationStatus.SUCCESS)
                .executionDetails(String.format("Global price updated to $%s, %d pending fees adjusted", newAmount, updatedFees))
                .build();
        
        feeGenerationLogRepository.save(logEntry);
        
        return new GlobalPriceUpdateResult(newAmount, updatedFees, "Global price updated successfully");
    }

    /**
     * Gets the current global fee price
     */
    public BigDecimal getCurrentGlobalFeePrice() {
        Optional<GlobalFeeConfiguration> config = globalFeeConfigurationRepository.findActiveConfiguration();
        return config.map(GlobalFeeConfiguration::getMonthlyFeeAmount).orElse(BigDecimal.ZERO);
    }

    /**
     * Generates fees for new members from current month onwards
     */
    @Transactional
    public void generateFeesForNewMember(Member member) {
        if (!"Protagonista".equals(member.getMemberType().getDescription())) {
            return; // Only protagonists pay monthly fees
        }
        
        LocalDate startDate = LocalDate.now();
        String currentMonth = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        try {
            processMemberMonthlyFee(member, currentMonth);
            log.info("Generated monthly fee for new member: {} {}", member.getName(), member.getLastname());
        } catch (Exception e) {
            log.error("Error generating fee for new member {}: {}", member.getId(), e.getMessage());
        }
    }

    /**
     * Sends email notifications to families about fee updates
     */
    private void sendFeeUpdateNotifications(BigDecimal newAmount) {
        try {
            // Get all family users (parents/guardians)
            List<User> familyUsers = userRepository.findByRoleDescription("FAMILY");
            
            for (User user : familyUsers) {
                try {
                    emailService.sendFeeUpdateNotification(user, newAmount);
                    Thread.sleep(100); // Small delay to avoid overwhelming the email server
                } catch (Exception e) {
                    log.error("Error sending fee update notification to user {}: {}", user.getEmail(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error sending bulk fee update notifications", e);
        }
    }

    /**
     * Get generation log for a specific month
     */
    public List<FeeGenerationLog> getGenerationLogForMonth(String month) {
        return feeGenerationLogRepository.findByGenerationMonthOrderByExecutionDateDesc(month);
    }

    /**
     * Check if fees have been generated for a specific month
     */
    public boolean hasFeesGeneratedForMonth(String month) {
        return feeGenerationLogRepository.countByGenerationMonth(month) > 0;
    }

    /**
     * Result class for fee generation operations
     */
    public static class FeeGenerationResult {
        private int membersProcessed = 0;
        private int feesGenerated = 0;
        private int feesUpdated = 0;
        private String error;

        public int getMembersProcessed() { return membersProcessed; }
        public void setMembersProcessed(int membersProcessed) { this.membersProcessed = membersProcessed; }

        public int getFeesGenerated() { return feesGenerated; }
        public void setFeesGenerated(int feesGenerated) { this.feesGenerated = feesGenerated; }
        public void incrementFeesGenerated() { this.feesGenerated++; }

        public int getFeesUpdated() { return feesUpdated; }
        public void setFeesUpdated(int feesUpdated) { this.feesUpdated = feesUpdated; }
        public void incrementFeesUpdated() { this.feesUpdated++; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public boolean hasError() { return error != null; }
    }

    /**
     * Result class for global price update operations
     */
    public static class GlobalPriceUpdateResult {
        private BigDecimal newPrice;
        private int updatedPendingFees;
        private String message;

        public GlobalPriceUpdateResult(BigDecimal newPrice, int updatedPendingFees, String message) {
            this.newPrice = newPrice;
            this.updatedPendingFees = updatedPendingFees;
            this.message = message;
        }

        public BigDecimal getNewPrice() { return newPrice; }
        public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }

        public int getUpdatedPendingFees() { return updatedPendingFees; }
        public void setUpdatedPendingFees(int updatedPendingFees) { this.updatedPendingFees = updatedPendingFees; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}