package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventRegistration;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Integer> {
    
    List<EventRegistration> findByEventIdOrderByRegistrationDateAsc(Integer eventId);
    
    List<EventRegistration> findByMemberIdOrderByRegistrationDateDesc(Integer memberId);
    
    Optional<EventRegistration> findByEventIdAndMemberId(Integer eventId, Integer memberId);
    
    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.eventId = :eventId AND er.status IN :statuses")
    Long countByEventIdAndStatusIn(@Param("eventId") Integer eventId, @Param("statuses") List<RegistrationStatus> statuses);
    
    List<EventRegistration> findByEventIdAndStatus(Integer eventId, RegistrationStatus status);
    
    boolean existsByEventIdAndMemberId(Integer eventId, Integer memberId);
}