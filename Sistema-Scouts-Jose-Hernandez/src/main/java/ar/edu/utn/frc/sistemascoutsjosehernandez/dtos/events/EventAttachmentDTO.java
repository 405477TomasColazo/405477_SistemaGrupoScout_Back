package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventAttachmentDTO {
    private Integer id;
    private Integer eventId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private LocalDateTime uploadedAt;
}
