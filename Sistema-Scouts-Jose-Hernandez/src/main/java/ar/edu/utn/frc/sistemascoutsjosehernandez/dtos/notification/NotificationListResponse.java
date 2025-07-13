package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.notification;

import lombok.Data;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class NotificationListResponse {
    private List<NotificationDto> notifications;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static NotificationListResponse fromPage(Page<NotificationDto> page) {
        return NotificationListResponse.builder()
                .notifications(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}