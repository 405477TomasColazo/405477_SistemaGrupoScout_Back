package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.notification.NotificationDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.notification.NotificationListResponse;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.notification.UnreadNotificationCountResponse;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.NotificationService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<NotificationListResponse> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notificationsPage = notificationService.getUserNotificationsPaged(currentUser, pageable);
        
        NotificationListResponse response = NotificationListResponse.fromPage(notificationsPage);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/unread")
    public ResponseEntity<UnreadNotificationCountResponse> getUnreadNotifications() {
        User currentUser = SecurityUtils.getCurrentUser();
        long count = notificationService.getUnreadNotificationCount(currentUser);
        
        UnreadNotificationCountResponse response = UnreadNotificationCountResponse.builder()
                .count(count)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        // Verify the notification belongs to the current user before marking as read
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer id) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        try {
            notificationService.deleteNotification(id, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}