package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessage;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ContactMessage entity with admin-focused queries
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    /**
     * Find all messages with pagination and sorting
     */
    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find messages by status
     */
    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessageStatus status);

    /**
     * Find messages by status with pagination
     */
    Page<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessageStatus status, Pageable pageable);

    /**
     * Find messages by type
     */
    Page<ContactMessage> findByMessageTypeOrderByCreatedAtDesc(ContactMessageType messageType, Pageable pageable);

    /**
     * Find unread messages (status = NEW)
     */
    List<ContactMessage> findByStatus(ContactMessageStatus status);

    /**
     * Count unread messages
     */
    long countByStatus(ContactMessageStatus status);

    /**
     * Find messages by date range
     */
    @Query("SELECT cm FROM ContactMessage cm WHERE cm.createdAt BETWEEN :startDate AND :endDate ORDER BY cm.createdAt DESC")
    Page<ContactMessage> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate, 
                                       Pageable pageable);

    /**
     * Search messages by name, email, subject, or message content
     */
    @Query("SELECT cm FROM ContactMessage cm WHERE " +
           "LOWER(cm.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cm.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cm.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cm.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY cm.createdAt DESC")
    Page<ContactMessage> searchMessages(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Advanced filter for admin interface
     */
    @Query("SELECT cm FROM ContactMessage cm WHERE " +
           "(:status IS NULL OR cm.status = :status) AND " +
           "(:messageType IS NULL OR cm.messageType = :messageType) AND " +
           "(:startDate IS NULL OR cm.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR cm.createdAt <= :endDate) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(cm.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cm.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cm.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY cm.createdAt DESC")
    Page<ContactMessage> findWithFilters(@Param("status") ContactMessageStatus status,
                                       @Param("messageType") ContactMessageType messageType,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("searchTerm") String searchTerm,
                                       Pageable pageable);

    /**
     * Find messages from specific email address
     */
    List<ContactMessage> findByEmailOrderByCreatedAtDesc(String email);

    /**
     * Get statistics for dashboard
     */
    @Query("SELECT " +
           "COUNT(cm) as total, " +
           "SUM(CASE WHEN cm.status = 'NEW' THEN 1 ELSE 0 END) as unread, " +
           "SUM(CASE WHEN cm.status = 'REPLIED' THEN 1 ELSE 0 END) as replied, " +
           "SUM(CASE WHEN cm.createdAt >= :sinceDate THEN 1 ELSE 0 END) as recent " +
           "FROM ContactMessage cm")
    Object[] getContactStatistics(@Param("sinceDate") LocalDateTime sinceDate);
}