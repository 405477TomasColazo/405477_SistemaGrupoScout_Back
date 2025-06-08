package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Notification;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.NotificationType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.Event;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.NotificationRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.NotificationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    
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
    
    private NotificationType getOrCreateNotificationType(String typeName) {
        return notificationTypeRepository.findByDescription(typeName)
                .orElseGet(() -> {
                    NotificationType newType = new NotificationType();
                    newType.setDescription(typeName);
                    return notificationTypeRepository.save(newType);
                });
    }
}