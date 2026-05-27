package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.notification.NotificationDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Notification;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.NotificationType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Fee;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Payment;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.Event;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.NotificationRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.NotificationTypeRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final FeeRepository feeRepository;
    
    public void createEventInvitationNotification(User user, Event event) {
        NotificationType invitationType = getOrCreateNotificationType("EVENT_INVITATION");
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Nueva invitación a evento");
        notification.setMessage(String.format("Has sido invitado al evento: %s. Fecha: %s", 
                event.getTitle(), 
                event.getStartDate().toString()));
        notification.setNotificationType(invitationType);
        notification.setCreatedAt(Instant.now());
        notification.setRelatedEntity("EVENT");
        notification.setRelatedEntityId(event.getId().intValue());
        
        notificationRepository.save(notification);
    }
    
    public void createEventUpdateNotification(User user, Event event) {
        NotificationType updateType = getOrCreateNotificationType("EVENT_UPDATE");
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Evento actualizado");
        notification.setMessage(String.format("El evento '%s' ha sido actualizado.", event.getTitle()));
        notification.setNotificationType(updateType);
        notification.setCreatedAt(Instant.now());
        notification.setRelatedEntity("EVENT");
        notification.setRelatedEntityId(event.getId().intValue());
        
        notificationRepository.save(notification);
    }
    
    public void createEventReminderNotification(User user, Event event) {
        NotificationType reminderType = getOrCreateNotificationType("EVENT_REMINDER");
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Recordatorio de evento");
        notification.setMessage(String.format("El evento '%s' se realizará pronto. Fecha: %s", 
                event.getTitle(), 
                event.getStartDate().toString()));
        notification.setNotificationType(reminderType);
        notification.setCreatedAt(Instant.now());
        notification.setRelatedEntity("EVENT");
        notification.setRelatedEntityId(event.getId().intValue());
        
        notificationRepository.save(notification);
    }
    
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadAtIsNullOrderByCreatedAtDesc(user);
    }
    
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
    }
    
    public Page<NotificationDto> getUserNotificationsPaged(User user, Pageable pageable) {
        Page<Notification> notificationsPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return notificationsPage.map(this::convertToDto);
    }
    
    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByUserAndReadAtIsNull(user);
    }
    
    public void deleteNotification(Integer notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own notifications");
        }
        
        notificationRepository.delete(notification);
    }
    
    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType().getDescription())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .relatedEntity(notification.getRelatedEntity())
                .relatedEntityId(notification.getRelatedEntityId())
                .read(notification.getReadAt() != null)
                .build();
    }
    
    public void createNewsAnnouncementNotification(User user, NewsArticle article) {
        NotificationType announcementType = getOrCreateNotificationType("NEWS_ANNOUNCEMENT");
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Nuevo anuncio disponible");
        notification.setMessage(String.format("Se ha publicado un nuevo anuncio: %s", article.getTitle()));
        notification.setNotificationType(announcementType);
        notification.setCreatedAt(Instant.now());
        notification.setRelatedEntity("NEWS");
        notification.setRelatedEntityId(article.getId());
        
        notificationRepository.save(notification);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }
    
    public void createMonthlyFeeGeneratedNotification(User user, String month, int feeCount) {
        NotificationType feeType = getOrCreateNotificationType("MONTHLY_FEE_GENERATED");
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Nuevas cuotas generadas");
        notification.setMessage(String.format("Se han generado %d cuota%s para el mes %s. Revisa tu panel de pagos.", 
                feeCount, feeCount > 1 ? "s" : "", month));
        notification.setNotificationType(feeType);
        notification.setCreatedAt(Instant.now());
        notification.setRelatedEntity("FEE");
        notification.setRelatedEntityId(null); // Multiple fees, no single ID
        
        notificationRepository.save(notification);
    }

    public void createPaymentReminderNotification(User user, Fee fee) {
        NotificationType reminderType = getOrCreateNotificationType("PAYMENT_REMINDER");
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Recordatorio de pago");
        notification.setMessage(String.format("Tienes una cuota pendiente de $%.2f del período %s. Te recomendamos realizar el pago pronto.", 
                fee.getAmount(), fee.getPeriod()));
        notification.setNotificationType(reminderType);
        notification.setCreatedAt(Instant.now());
        notification.setRelatedEntity("FEE");
        notification.setRelatedEntityId(fee.getId());
        
        notificationRepository.save(notification);
    }

    public void createPaymentFailedNotification(User user, Payment payment, String reason) {
        NotificationType failedType = getOrCreateNotificationType("PAYMENT_FAILED");
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Pago rechazado");
        notification.setMessage(String.format("Tu pago de $%.2f no pudo procesarse. Motivo: %s. Intenta nuevamente con otro método.", 
                payment.getAmount(), reason));
        notification.setNotificationType(failedType);
        notification.setCreatedAt(Instant.now());
        notification.setRelatedEntity("PAYMENT");
        notification.setRelatedEntityId(payment.getId());
        
        notificationRepository.save(notification);
    }

    public void createBalanceAppliedNotification(User user, int feesPaid, BigDecimal balanceUsed) {
        NotificationType balanceType = getOrCreateNotificationType("BALANCE_APPLIED");
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Balance aplicado exitosamente");
        notification.setMessage(String.format("Se aplicó $%.2f de tu balance a %d cuota%s. ¡Gracias por mantener tus pagos al día!", 
                balanceUsed, feesPaid, feesPaid > 1 ? "s" : ""));
        notification.setNotificationType(balanceType);
        notification.setCreatedAt(Instant.now());
        notification.setRelatedEntity("BALANCE");
        notification.setRelatedEntityId(null);
        
        notificationRepository.save(notification);
    }

    /**
     * Automatically send payment reminders for overdue fees
     * Runs every day at 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendPaymentReminders() {
        try {
            log.info("Starting automatic payment reminders process");
            
            // Get current month (fees from previous months are considered overdue)
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            // Find overdue fees (period has passed and status is still PENDING)
            List<Fee> overdueFees = feeRepository.findOverdueFees(currentMonth);
            
            log.info("Found {} overdue fees to send reminders for", overdueFees.size());
            
            int remindersSent = 0;
            for (Fee fee : overdueFees) {
                try {
                    // Check if reminder was already sent in the last 7 days to avoid spam
                    if (shouldSendReminder(fee)) {
                        Member member = fee.getMember();
                        if (member != null && member.getUser() != null) {
                            createPaymentReminderNotification(member.getUser(), fee);
                            remindersSent++;
                        }
                    }
                } catch (Exception e) {
                    log.error("Error sending reminder for fee {}: {}", fee.getId(), e.getMessage());
                }
            }
            
            log.info("Payment reminders process completed. Sent {} reminders", remindersSent);
            
        } catch (Exception e) {
            log.error("Error in automatic payment reminders process", e);
        }
    }
    
    /**
     * Check if a reminder should be sent for this fee
     * Avoid sending multiple reminders in a short period
     */
    private boolean shouldSendReminder(Fee fee) {
        try {
            // Check if a reminder notification was already sent in the last 7 days
            List<Notification> recentReminders = notificationRepository.findRecentReminderNotifications(
                fee.getMember().getUser(), 
                fee.getId(), 
                Instant.now().minusSeconds(7 * 24 * 60 * 60) // 7 days ago
            );
            
            return recentReminders.isEmpty();
        } catch (Exception e) {
            log.error("Error checking reminder history for fee {}: {}", fee.getId(), e.getMessage());
            return false; // Don't send if there's an error
        }
    }

    public NotificationType getOrCreateNotificationType(String typeName) {
        return notificationTypeRepository.findByDescription(typeName)
                .orElseGet(() -> {
                    NotificationType newType = new NotificationType();
                    newType.setDescription(typeName);
                    return notificationTypeRepository.save(newType);
                });
    }
}