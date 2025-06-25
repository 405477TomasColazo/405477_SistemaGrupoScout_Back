package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.FeeGenerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeGenerationLogRepository extends JpaRepository<FeeGenerationLog, Integer> {
    
    Optional<FeeGenerationLog> findByGenerationMonth(String generationMonth);
    
    List<FeeGenerationLog> findByGenerationMonthOrderByExecutionDateDesc(String generationMonth);
    
    Page<FeeGenerationLog> findAllByOrderByExecutionDateDesc(Pageable pageable);
    
    @Query("SELECT fgl FROM FeeGenerationLog fgl " +
           "WHERE fgl.executionDate BETWEEN :fromDate AND :toDate " +
           "ORDER BY fgl.executionDate DESC")
    List<FeeGenerationLog> findByExecutionDateBetween(
            @Param("fromDate") LocalDateTime fromDate, 
            @Param("toDate") LocalDateTime toDate);
    
    @Query("SELECT fgl FROM FeeGenerationLog fgl " +
           "WHERE fgl.status = :status " +
           "ORDER BY fgl.executionDate DESC")
    List<FeeGenerationLog> findByStatus(@Param("status") FeeGenerationLog.FeeGenerationStatus status);
    
    @Query("SELECT COUNT(fgl) FROM FeeGenerationLog fgl WHERE fgl.generationMonth = :month")
    long countByGenerationMonth(@Param("month") String month);
    
    @Query("SELECT fgl FROM FeeGenerationLog fgl " +
           "WHERE (:generationType IS NULL OR :generationType = '' OR fgl.executionType = :generationType) " +
           "ORDER BY fgl.executionDate DESC")
    Page<FeeGenerationLog> findWithFilters(
            @Param("generationType") String generationType,
            Pageable pageable);
}