package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsDistribution;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NewsDistributionDto {
    private Integer id;
    private Integer articleId;
    private String articleTitle;
    private LocalDateTime sentAt;
    private String sentByName;
    private int totalRecipients;
    private NewsDistribution.DistributionStatus status;
    private List<DistributionFilterDto> filters;
    
    @Data
    @Builder
    public static class DistributionFilterDto {
        private String filterType;
        private String filterValue;
        private String filterDescription;
    }
}