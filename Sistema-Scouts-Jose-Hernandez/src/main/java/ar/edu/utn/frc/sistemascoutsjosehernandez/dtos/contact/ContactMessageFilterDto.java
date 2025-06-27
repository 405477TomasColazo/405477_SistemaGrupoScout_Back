package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for filtering contact messages in admin interface
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessageFilterDto {

    private ContactMessageStatus status;
    private ContactMessageType messageType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String searchTerm;

    // Pagination parameters
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "desc";
}