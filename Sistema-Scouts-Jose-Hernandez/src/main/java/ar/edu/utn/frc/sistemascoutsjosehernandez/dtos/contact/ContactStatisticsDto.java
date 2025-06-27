package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for contact message statistics for admin dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactStatisticsDto {

    private long totalMessages;
    private long unreadMessages;
    private long repliedMessages;
    private long recentMessages; // Messages from last 7 days
    private long archivedMessages;

    /**
     * Calculate percentage of replied messages
     */
    public double getReplyRate() {
        if (totalMessages == 0) return 0.0;
        return (double) repliedMessages / totalMessages * 100;
    }

    /**
     * Calculate percentage of unread messages
     */
    public double getUnreadRate() {
        if (totalMessages == 0) return 0.0;
        return (double) unreadMessages / totalMessages * 100;
    }
}