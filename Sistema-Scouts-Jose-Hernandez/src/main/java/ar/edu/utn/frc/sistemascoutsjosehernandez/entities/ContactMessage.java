package ar.edu.utn.frc.sistemascoutsjosehernandez.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity to store contact form submissions from the website
 */
@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private ContactMessageType messageType = ContactMessageType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ContactMessageStatus status = ContactMessageStatus.NEW;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    /**
     * Helper method to check if message is unread
     */
    public boolean isUnread() {
        return status == ContactMessageStatus.NEW;
    }

    /**
     * Helper method to mark message as read
     */
    public void markAsRead() {
        if (status == ContactMessageStatus.NEW) {
            status = ContactMessageStatus.READ;
        }
    }

    /**
     * Helper method to mark message as replied
     */
    public void markAsReplied() {
        status = ContactMessageStatus.REPLIED;
        repliedAt = LocalDateTime.now();
    }
}