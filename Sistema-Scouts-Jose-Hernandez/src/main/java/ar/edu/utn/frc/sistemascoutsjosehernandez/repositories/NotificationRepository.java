package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Notification;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<Notification> findByUserAndReadAtIsNullOrderByCreatedAtDesc(User user);
    
    long countByUserAndReadAtIsNull(User user);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.relatedEntity = :entity AND n.relatedEntityId = :entityId")
    List<Notification> findByUserAndRelatedEntity(@Param("user") User user, 
                                                  @Param("entity") String entity, 
                                                  @Param("entityId") Integer entityId);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.notificationType.description = 'PAYMENT_REMINDER' AND n.relatedEntityId = :feeId AND n.createdAt >= :since")
    List<Notification> findRecentReminderNotifications(@Param("user") User user, 
                                                       @Param("feeId") Integer feeId, 
                                                       @Param("since") Instant since);
}