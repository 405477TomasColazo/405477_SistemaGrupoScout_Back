package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for contact message responses (read operations)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessageDto {

    private Long id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private ContactMessageType messageType;
    private ContactMessageStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String adminNotes;
    private LocalDateTime repliedAt;

    /**
     * Helper method to check if message is unread
     */
    public boolean isUnread() {
        return status == ContactMessageStatus.NEW;
    }

    /**
     * Get formatted creation date for display
     */
    public String getFormattedCreatedAt() {
        if (createdAt != null) {
            return createdAt.toString(); // Could be formatted as needed
        }
        return "";
    }

    /**
     * Get status display name in Spanish
     */
    public String getStatusDisplayName() {
        if (status == null) return "";
        
        return switch (status) {
            case NEW -> "Nuevo";
            case READ -> "Leído";
            case REPLIED -> "Respondido";
            case ARCHIVED -> "Archivado";
        };
    }

    /**
     * Get message type display name in Spanish
     */
    public String getMessageTypeDisplayName() {
        if (messageType == null) return "";
        
        return switch (messageType) {
            case GENERAL -> "General";
            case MEMBERSHIP -> "Membresía";
            case EVENTS -> "Eventos";
            case COMPLAINT -> "Queja";
            case SUGGESTION -> "Sugerencia";
            case TECHNICAL -> "Técnico";
            case OTHER -> "Otro";
        };
    }
}