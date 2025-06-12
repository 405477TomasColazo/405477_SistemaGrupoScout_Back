package ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class NewsArticleDto {
    private Integer id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String featuredImage;
    private String authorName;
    private NewsArticle.NewsStatus status;
    private LocalDateTime publishDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewsCount;
    private Set<NewsCategoryDto> categories;
}