package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class NewsArticleSummaryDto {
    private Integer id;
    private String title;
    private String slug;
    private String summary;
    private String featuredImage;
    private String authorName;
    private NewsArticle.NewsStatus status;
    private LocalDateTime publishDate;
    private int viewsCount;
    private Set<NewsCategoryDto> categories;
}