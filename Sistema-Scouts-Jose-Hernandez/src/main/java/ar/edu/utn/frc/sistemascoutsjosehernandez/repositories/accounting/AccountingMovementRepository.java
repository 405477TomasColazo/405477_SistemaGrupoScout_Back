package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.Account;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.AccountingMovement;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountingMovementRepository extends JpaRepository<AccountingMovement, Long> {
    
    List<AccountingMovement> findByAccountOrderByAccountingEntry_EntryDateDesc(Account account);
    
    @Query("SELECT am FROM AccountingMovement am " +
           "JOIN am.accountingEntry ae " +
           "WHERE am.account = :account " +
           "AND ae.entryDate BETWEEN :startDate AND :endDate " +
           "AND ae.status = 'ACTIVE' " +
           "ORDER BY ae.entryDate DESC, ae.entryNumber DESC")
    List<AccountingMovement> findByAccountAndDateRange(@Param("account") Account account,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT am FROM AccountingMovement am " +
           "JOIN am.accountingEntry ae " +
           "WHERE (:accountId IS NULL OR am.account.id = :accountId) " +
           "AND (:movementType IS NULL OR am.movementType = :movementType) " +
           "AND (:dateFrom IS NULL OR ae.entryDate >= :dateFrom) " +
           "AND (:dateTo IS NULL OR ae.entryDate <= :dateTo) " +
           "AND (:minAmount IS NULL OR am.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR am.amount <= :maxAmount) " +
           "AND ae.status = 'ACTIVE' " +
           "ORDER BY ae.entryDate DESC, ae.entryNumber DESC")
    Page<AccountingMovement> findMovementsWithFilters(@Param("accountId") Long accountId,
                                                     @Param("movementType") MovementType movementType,
                                                     @Param("dateFrom") LocalDate dateFrom,
                                                     @Param("dateTo") LocalDate dateTo,
                                                     @Param("minAmount") BigDecimal minAmount,
                                                     @Param("maxAmount") BigDecimal maxAmount,
                                                     Pageable pageable);
    
    @Query("SELECT SUM(CASE WHEN am.movementType = 'DEBIT' THEN am.amount ELSE -am.amount END) " +
           "FROM AccountingMovement am " +
           "JOIN am.accountingEntry ae " +
           "WHERE am.account = :account " +
           "AND ae.entryDate <= :asOfDate " +
           "AND ae.status = 'ACTIVE'")
    BigDecimal getAccountBalance(@Param("account") Account account,
                                @Param("asOfDate") LocalDate asOfDate);
    
    @Query("SELECT am.account.id, am.account.code, am.account.name, " +
           "SUM(CASE WHEN am.movementType = 'DEBIT' THEN am.amount ELSE 0 END) as totalDebits, " +
           "SUM(CASE WHEN am.movementType = 'CREDIT' THEN am.amount ELSE 0 END) as totalCredits, " +
           "SUM(CASE WHEN am.movementType = 'DEBIT' THEN am.amount ELSE -am.amount END) as balance " +
           "FROM AccountingMovement am " +
           "JOIN am.accountingEntry ae " +
           "WHERE ae.entryDate BETWEEN :startDate AND :endDate " +
           "AND ae.status = 'ACTIVE' " +
           "GROUP BY am.account.id, am.account.code, am.account.name " +
           "ORDER BY am.account.code")
    List<Object[]> getAccountBalanceReport(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(DISTINCT am.account.id) FROM AccountingMovement am " +
           "JOIN am.accountingEntry ae " +
           "WHERE ae.status = 'ACTIVE'")
    Long countAccountsWithMovements();
}