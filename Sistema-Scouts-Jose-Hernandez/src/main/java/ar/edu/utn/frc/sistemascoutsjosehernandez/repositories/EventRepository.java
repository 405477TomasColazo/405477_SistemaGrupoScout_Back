package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.Event;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    
    @Query("SELECT e FROM Event e WHERE " +
           "(:sections IS NULL OR EXISTS (SELECT s FROM e.sections s WHERE s IN :sections)) AND " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:dateFrom IS NULL OR e.startDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR e.endDate <= :dateTo) AND " +
           "(:createdBy IS NULL OR e.createdBy = :createdBy) " +
           "ORDER BY e.startDate ASC")
    List<Event> findEventsWithFilters(
            @Param("sections") Set<String> sections,
            @Param("eventType") EventType eventType,
            @Param("status") EventStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("createdBy") Integer createdBy
    );
    
    List<Event> findByCreatedByOrderByStartDateAsc(Integer createdBy);
    
    List<Event> findByStatusOrderByStartDateAsc(EventStatus status);
    
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :startDate AND :endDate ORDER BY e.startDate ASC")
    List<Event> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}