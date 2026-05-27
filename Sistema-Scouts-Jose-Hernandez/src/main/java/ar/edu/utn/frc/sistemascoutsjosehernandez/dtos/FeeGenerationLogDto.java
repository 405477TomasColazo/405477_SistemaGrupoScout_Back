package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.FeeGenerationLog;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeGenerationLogDto {
    
    private Integer id;
    
    @JsonProperty("generationType")
    private String generationType;
    
    @JsonProperty("executedAt")
    private LocalDateTime executedAt;
    
    private Integer totalMembersProcessed;
    
    private Integer totalFeesGenerated;
    
    private Integer totalFeesUpdated;
    
    @JsonProperty("targetMonth")
    private String targetMonth;
    
    private String section;
    
    private Integer memberId;
    
    private String memberName;
    
    @JsonProperty("details")
    private String details;
    
    private UserDto executedByUser;
    
    private String status;
    
    // Mapper methods
    public static FeeGenerationLogDto fromEntity(FeeGenerationLog entity) {
        if (entity == null) {
            return null;
        }
        
        return FeeGenerationLogDto.builder()
            .id(entity.getId())
            .generationType(entity.getExecutionType())
            .executedAt(entity.getExecutionDate())
            .totalMembersProcessed(entity.getTotalMembersProcessed())
            .totalFeesGenerated(entity.getTotalFeesGenerated())
            .totalFeesUpdated(entity.getTotalFeesUpdated())
            .targetMonth(entity.getGenerationMonth())
            .details(entity.getExecutionDetails())
            .status(entity.getStatus() != null ? entity.getStatus().name() : null)
            .executedByUser(UserDto.fromEntity(entity.getExecutedByUser()))
            .build();
    }
}