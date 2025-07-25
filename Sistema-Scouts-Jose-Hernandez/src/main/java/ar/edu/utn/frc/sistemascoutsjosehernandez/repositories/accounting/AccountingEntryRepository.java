package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.AccountingEntry;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.EntryStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting.OriginType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {
    
    Optional<AccountingEntry> findByEntryNumber(Integer entryNumber);
    
    Optional<AccountingEntry> findByOriginReference(String originReference);
    
    List<AccountingEntry> findByStatusOrderByEntryDateDesc(EntryStatus status);
    
    @Query("SELECT MAX(ae.entryNumber) FROM AccountingEntry ae")
    Optional<Integer> findMaxEntryNumber();
    
    @Query("SELECT ae FROM AccountingEntry ae WHERE " +
           "(:dateFrom IS NULL OR ae.entryDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR ae.entryDate <= :dateTo) AND " +
           "(:status IS NULL OR ae.status = :status) AND " +
           "(:originType IS NULL OR ae.originType = :originType) AND " +
           "(:description IS NULL OR UPPER(ae.description) LIKE UPPER(CONCAT('%', :description, '%'))) " +
           "ORDER BY ae.entryDate DESC, ae.entryNumber DESC")
    Page<AccountingEntry> findEntriesWithFilters(@Param("dateFrom") LocalDate dateFrom,
                                                @Param("dateTo") LocalDate dateTo,
                                                @Param("status") EntryStatus status,
                                                @Param("originType") OriginType originType,
                                                @Param("description") String description,
                                                Pageable pageable);
    
    @Query("SELECT ae FROM AccountingEntry ae " +
           "LEFT JOIN FETCH ae.movements m " +
           "LEFT JOIN FETCH m.account a " +
           "WHERE ae.id = :id")
    Optional<AccountingEntry> findByIdWithMovements(@Param("id") Long id);
    
    @Query("SELECT ae FROM AccountingEntry ae WHERE " +
           "ae.entryDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ae.entryDate, ae.entryNumber")
    List<AccountingEntry> findEntriesForPeriod(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(ae) FROM AccountingEntry ae WHERE ae.status = 'ACTIVE'")
    Long countActiveEntries();
    
    @Query("SELECT SUM(CASE WHEN m.movementType = 'DEBIT' THEN m.amount ELSE 0 END) as totalDebits, " +
           "SUM(CASE WHEN m.movementType = 'CREDIT' THEN m.amount ELSE 0 END) as totalCredits " +
           "FROM AccountingEntry ae " +
           "JOIN ae.movements m " +
           "WHERE ae.entryDate BETWEEN :startDate AND :endDate " +
           "AND ae.status = 'ACTIVE'")
    Object[] getTotalsForPeriod(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);
}