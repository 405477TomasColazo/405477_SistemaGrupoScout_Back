package ar.edu.utn.frc.sistemascoutsjosehernandez.services.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.accounting.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.UserRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.accounting.AccountRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.accounting.AccountingEntryRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.accounting.AccountingMovementRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountingService {

    private final AccountRepository accountRepository;
    private final AccountingEntryRepository entryRepository;
    private final AccountingMovementRepository movementRepository;
    private final UserRepository userRepository;

    // Account management methods
    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAllActiveOrderByCode()
                .stream()
                .map(this::mapToAccountDto)
                .collect(Collectors.toList());
    }

    public List<AccountDto> getAccountsByType(AccountType type) {
        return accountRepository.findByTypeActiveOrderByCode(type)
                .stream()
                .map(this::mapToAccountDto)
                .collect(Collectors.toList());
    }

    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        return mapToAccountDto(account);
    }

    public AccountDto getAccountByCode(String code) {
        Account account = accountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Account not found with code: " + code));
        return mapToAccountDto(account);
    }

    // Accounting entry management methods
    public Page<AccountingEntryDto> getAccountingEntries(AccountingFiltersDto filters, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        Page<AccountingEntry> entries = entryRepository.findEntriesWithFilters(
                filters.getDateFrom(),
                filters.getDateTo(),
                filters.getStatus(),
                filters.getOriginType(),
                filters.getDescription(),
                filters.getAccountId(),
                pageable
        );

        return entries.map(this::mapToAccountingEntryDto);
    }

    public AccountingEntryDto getAccountingEntryById(Long id) {
        AccountingEntry entry = entryRepository.findByIdWithMovements(id)
                .orElseThrow(() -> new RuntimeException("Accounting entry not found with id: " + id));
        return mapToAccountingEntryDto(entry);
    }

    @Transactional
    public AccountingEntryDto createManualAccountingEntry(CreateAccountingEntryDto createDto) {
        // Validate double-entry accounting (debits = credits)
        validateDoubleEntryBalance(createDto.getMovements());

        // Get current user
        User currentUser = getCurrentUser();

        // Get next entry number
        Integer nextEntryNumber = getNextEntryNumber();

        // Create accounting entry
        AccountingEntry entry = AccountingEntry.builder()
                .entryNumber(nextEntryNumber)
                .entryDate(createDto.getEntryDate())
                .description(createDto.getDescription())
                .originType(OriginType.MANUAL)
                .status(EntryStatus.ACTIVE)
                .createdByUser(currentUser)
                .build();

        // Create movements
        List<AccountingMovement> movements = createDto.getMovements().stream()
                .map(movementDto -> {
                    Account account = accountRepository.findById(movementDto.getAccountId())
                            .orElseThrow(() -> new RuntimeException("Account not found with id: " + movementDto.getAccountId()));

                    return AccountingMovement.builder()
                            .accountingEntry(entry)
                            .account(account)
                            .movementType(movementDto.getMovementType())
                            .amount(movementDto.getAmount())
                            .description(movementDto.getDescription())
                            .build();
                })
                .collect(Collectors.toList());

        entry.setMovements(movements);

        // Save entry
        AccountingEntry savedEntry = entryRepository.save(entry);
        
        log.info("Created manual accounting entry #{} by user {}", nextEntryNumber, currentUser.getEmail());
        
        return mapToAccountingEntryDto(savedEntry);
    }

    @Transactional
    public AccountingEntryDto createAutomaticPaymentEntry(String paymentId, BigDecimal amount, 
                                                         String description, String paymentMethod) {
        // Check if entry already exists for this payment
        Optional<AccountingEntry> existingEntry = entryRepository.findByOriginReference(paymentId);
        if (existingEntry.isPresent()) {
            log.warn("Accounting entry already exists for payment: {}", paymentId);
            return mapToAccountingEntryDto(existingEntry.get());
        }

        // Get system user for automatic entries
        User systemUser = getSystemUser();

        // Get next entry number
        Integer nextEntryNumber = getNextEntryNumber();

        // Determine accounts based on payment method
        Account debitAccount = getPaymentMethodAccount(paymentMethod);
        Account creditAccount = getIncomeAccount(description);

        // Create accounting entry
        AccountingEntry entry = AccountingEntry.builder()
                .entryNumber(nextEntryNumber)
                .entryDate(LocalDate.now())
                .description(description)
                .originType(OriginType.AUTOMATIC)
                .originReference(paymentId)
                .status(EntryStatus.ACTIVE)
                .createdByUser(systemUser)
                .build();

        // Create movements (debit cash/bank, credit income)
        List<AccountingMovement> movements = List.of(
                AccountingMovement.builder()
                        .accountingEntry(entry)
                        .account(debitAccount)
                        .movementType(MovementType.DEBIT)
                        .amount(amount)
                        .description("Pago recibido - " + paymentMethod)
                        .build(),
                AccountingMovement.builder()
                        .accountingEntry(entry)
                        .account(creditAccount)
                        .movementType(MovementType.CREDIT)
                        .amount(amount)
                        .description("Ingreso por " + description)
                        .build()
        );

        entry.setMovements(movements);

        // Save entry
        AccountingEntry savedEntry = entryRepository.save(entry);
        
        log.info("Created automatic accounting entry #{} for payment {}", nextEntryNumber, paymentId);
        
        return mapToAccountingEntryDto(savedEntry);
    }

    @Transactional
    public AccountingEntryDto cancelAccountingEntry(Long entryId, String reason) {
        AccountingEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Accounting entry not found with id: " + entryId));

        if (entry.getStatus() == EntryStatus.CANCELLED) {
            throw new RuntimeException("Accounting entry is already cancelled");
        }

        if (entry.getOriginType() == OriginType.AUTOMATIC) {
            throw new RuntimeException("Cannot cancel automatic accounting entries manually");
        }

        // Update entry status
        entry.setStatus(EntryStatus.CANCELLED);
        entry.setModifiedByUser(getCurrentUser());

        // Create cancellation entry
        Integer nextEntryNumber = getNextEntryNumber();
        
        AccountingEntry cancellationEntry = AccountingEntry.builder()
                .entryNumber(nextEntryNumber)
                .entryDate(LocalDate.now())
                .description("Anulación de asiento #" + entry.getEntryNumber() + " - " + reason)
                .originType(OriginType.MANUAL)
                .status(EntryStatus.ACTIVE)
                .createdByUser(getCurrentUser())
                .build();

        // Create reverse movements
        List<AccountingMovement> reverseMovements = entry.getMovements().stream()
                .map(originalMovement -> AccountingMovement.builder()
                        .accountingEntry(cancellationEntry)
                        .account(originalMovement.getAccount())
                        .movementType(originalMovement.getMovementType() == MovementType.DEBIT ? 
                                     MovementType.CREDIT : MovementType.DEBIT)
                        .amount(originalMovement.getAmount())
                        .description("Reversión: " + originalMovement.getDescription())
                        .build())
                .collect(Collectors.toList());

        cancellationEntry.setMovements(reverseMovements);

        // Save both entries
        entryRepository.save(entry);
        AccountingEntry savedCancellationEntry = entryRepository.save(cancellationEntry);

        log.info("Cancelled accounting entry #{} and created reversal entry #{}", 
                entry.getEntryNumber(), nextEntryNumber);

        return mapToAccountingEntryDto(savedCancellationEntry);
    }

    // Report methods
    public AccountingReportDto generateJournalReport(LocalDate startDate, LocalDate endDate) {
        List<AccountingEntry> entries = entryRepository.findEntriesForPeriod(startDate, endDate);
        Object[] totals = entryRepository.getTotalsForPeriod(startDate, endDate);

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        if (totals != null && totals.length > 0 && totals[0] instanceof Object[]) {
            Object[] values = (Object[]) totals[0];

            if (values.length >= 2) {
                if (values[0] != null) {
                    totalDebits = values[0] instanceof BigDecimal ? (BigDecimal) values[0] :
                            new BigDecimal(values[0].toString());
                }
                if (values[1] != null) {
                    totalCredits = values[1] instanceof BigDecimal ? (BigDecimal) values[1] :
                            new BigDecimal(values[1].toString());
                }
            }
        }


        return AccountingReportDto.builder()
                .reportType("JOURNAL")
                .startDate(startDate)
                .endDate(endDate)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .balanceDifference(totalDebits.subtract(totalCredits))
                .entries(entries.stream().map(this::mapToAccountingEntryDto).collect(Collectors.toList()))
                .build();
    }

    public AccountingReportDto generateBalanceReport(LocalDate startDate, LocalDate endDate) {
        List<Object[]> balanceData = movementRepository.getAccountBalanceReport(startDate, endDate);
        
        List<AccountingReportDto.AccountBalanceDto> accountBalances = balanceData.stream()
                .map(data -> AccountingReportDto.AccountBalanceDto.builder()
                        .accountId((Long) data[0])
                        .accountCode((String) data[1])
                        .accountName((String) data[2])
                        .totalDebits(data[3] instanceof BigDecimal ? (BigDecimal) data[3] : 
                                   data[3] != null ? new BigDecimal(data[3].toString()) : BigDecimal.ZERO)
                        .totalCredits(data[4] instanceof BigDecimal ? (BigDecimal) data[4] : 
                                    data[4] != null ? new BigDecimal(data[4].toString()) : BigDecimal.ZERO)
                        .balance(data[5] instanceof BigDecimal ? (BigDecimal) data[5] : 
                               data[5] != null ? new BigDecimal(data[5].toString()) : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalDebits = accountBalances.stream()
                .map(AccountingReportDto.AccountBalanceDto::getTotalDebits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = accountBalances.stream()
                .map(AccountingReportDto.AccountBalanceDto::getTotalCredits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AccountingReportDto.builder()
                .reportType("BALANCE")
                .startDate(startDate)
                .endDate(endDate)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .balanceDifference(totalDebits.subtract(totalCredits))
                .accountBalances(accountBalances)
                .build();
    }

    // Helper methods
    private void validateDoubleEntryBalance(List<CreateAccountingMovementDto> movements) {
        BigDecimal totalDebits = movements.stream()
                .filter(m -> m.getMovementType() == MovementType.DEBIT)
                .map(CreateAccountingMovementDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = movements.stream()
                .filter(m -> m.getMovementType() == MovementType.CREDIT)
                .map(CreateAccountingMovementDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new RuntimeException("Double-entry validation failed: debits (" + totalDebits + 
                                     ") must equal credits (" + totalCredits + ")");
        }
    }

    private Integer getNextEntryNumber() {
        return entryRepository.findMaxEntryNumber().orElse(0) + 1;
    }

    private User getCurrentUser() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private User getSystemUser() {
        // Create or get system user for automatic entries
        return userRepository.findByEmail("system@scouts.com")
                .orElseGet(() -> {
                    // In a real implementation, you'd have a proper system user
                    // For now, we'll use the current user or create a placeholder
                    try {
                        return getCurrentUser();
                    } catch (Exception e) {
                        throw new RuntimeException("System user not configured for automatic entries");
                    }
                });
    }

    private Account getPaymentMethodAccount(String paymentMethod) {
        // Map payment methods to accounts
        String accountCode = switch (paymentMethod.toLowerCase()) {
            case "card", "credit_card", "debit_card" -> "1.1.2"; // Bank account
            case "cash", "ticket" -> "1.1.1"; // Cash account
            case "bank_transfer", "wire_transfer" -> "1.1.2"; // Bank account
            default -> "1.1.2"; // Default to bank
        };

        return accountRepository.findByCode(accountCode)
                .orElseThrow(() -> new RuntimeException("Payment method account not found: " + accountCode));
    }

    private Account getIncomeAccount(String description) {
        // Determine income account based on description
        String accountCode;
        if (description.toLowerCase().contains("cuota") || description.toLowerCase().contains("monthly")) {
            accountCode = "4.1.1"; // Monthly fees income
        } else if (description.toLowerCase().contains("evento") || description.toLowerCase().contains("event")) {
            accountCode = "4.1.2"; // Event income
        } else {
            accountCode = "4.1.1"; // Default to monthly fees
        }

        return accountRepository.findByCode(accountCode)
                .orElseThrow(() -> new RuntimeException("Income account not found: " + accountCode));
    }

    // Mapping methods
    private AccountDto mapToAccountDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .type(account.getType())
                .active(account.getActive())
                .description(account.getDescription())
                .build();
    }

    private AccountingEntryDto mapToAccountingEntryDto(AccountingEntry entry) {
        List<AccountingMovementDto> movementDtos = entry.getMovements().stream()
                .map(this::mapToAccountingMovementDto)
                .collect(Collectors.toList());

        BigDecimal totalDebits = movementDtos.stream()
                .filter(m -> m.getMovementType() == MovementType.DEBIT)
                .map(AccountingMovementDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = movementDtos.stream()
                .filter(m -> m.getMovementType() == MovementType.CREDIT)
                .map(AccountingMovementDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AccountingEntryDto.builder()
                .id(entry.getId())
                .entryNumber(entry.getEntryNumber())
                .entryDate(entry.getEntryDate())
                .description(entry.getDescription())
                .originType(entry.getOriginType())
                .originReference(entry.getOriginReference())
                .status(entry.getStatus())
                .createdByUserName(entry.getCreatedByUser() != null ? 
                                  entry.getCreatedByUser().getEmail() : null)
                .createdAt(entry.getCreatedAt())
                .modifiedByUserName(entry.getModifiedByUser() != null ? 
                                   entry.getModifiedByUser().getEmail() : null)
                .modifiedAt(entry.getModifiedAt())
                .movements(movementDtos)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .build();
    }

    private AccountingMovementDto mapToAccountingMovementDto(AccountingMovement movement) {
        return AccountingMovementDto.builder()
                .id(movement.getId())
                .accountId(movement.getAccount().getId())
                .accountCode(movement.getAccount().getCode())
                .accountName(movement.getAccount().getName())
                .movementType(movement.getMovementType())
                .amount(movement.getAmount())
                .description(movement.getDescription())
                .build();
    }
}