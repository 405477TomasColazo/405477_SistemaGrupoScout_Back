package ar.edu.utn.frc.sistemascoutsjosehernandez.services.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsCategory;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsArticleRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsCategoryRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final NewsCategoryRepository newsCategoryRepository;

    @Transactional(readOnly = true)
    public Page<NewsArticleSummaryDto> getPublishedArticles(Pageable pageable) {
        return newsArticleRepository.findByStatusOrderByPublishDateDesc(
                NewsArticle.NewsStatus.PUBLISHED, 
                pageable
        ).map(this::convertToSummaryDto);
    }

    @Transactional(readOnly = true)
    public List<NewsArticleSummaryDto> getLatestArticles(int limit) {
        return newsArticleRepository.findLatestPublishedArticles(
                Pageable.ofSize(limit)
        ).stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<NewsArticleDto> getArticleBySlug(String slug) {
        return newsArticleRepository.findBySlug(slug)
                .filter(article -> article.getStatus() == NewsArticle.NewsStatus.PUBLISHED)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public Optional<NewsArticleDto> getArticleByIdAdmin(Integer id) {
        return newsArticleRepository.findById(id).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public Page<NewsArticleSummaryDto> getAllArticlesAdmin(Pageable pageable) {
        return newsArticleRepository.findAll(pageable).map(this::convertToSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<NewsArticleSummaryDto> searchPublishedArticles(String searchTerm, Pageable pageable) {
        return newsArticleRepository.searchPublishedArticles(searchTerm, pageable)
                .map(this::convertToSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<NewsArticleSummaryDto> getArticlesByCategory(Integer categoryId, Pageable pageable) {
        return newsArticleRepository.findByCategoryIdAndStatus(categoryId, pageable)
                .map(this::convertToSummaryDto);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public NewsArticleDto createArticle(CreateNewsArticleDto createDto) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        NewsArticle article = NewsArticle.builder()
                .title(createDto.getTitle())
                .slug(generateUniqueSlug(createDto.getTitle()))
                .summary(createDto.getSummary())
                .content(sanitizeContent(createDto.getContent()))
                .featuredImage(createDto.getFeaturedImage())
                .author(currentUser)
                .status(NewsArticle.NewsStatus.DRAFT)
                .build();

        if (createDto.getCategoryIds() != null && !createDto.getCategoryIds().isEmpty()) {
            Set<NewsCategory> categories = newsCategoryRepository.findAllById(createDto.getCategoryIds())
                    .stream().collect(Collectors.toSet());
            article.setCategories(categories);
        }

        NewsArticle savedArticle = newsArticleRepository.save(article);
        return convertToDto(savedArticle);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public NewsArticleDto updateArticle(Integer id, UpdateNewsArticleDto updateDto) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        if (updateDto.getTitle() != null) {
            article.setTitle(updateDto.getTitle());
            article.setSlug(generateUniqueSlug(updateDto.getTitle()));
        }
        if (updateDto.getSummary() != null) {
            article.setSummary(updateDto.getSummary());
        }
        if (updateDto.getContent() != null) {
            article.setContent(sanitizeContent(updateDto.getContent()));
        }
        if (updateDto.getFeaturedImage() != null) {
            article.setFeaturedImage(updateDto.getFeaturedImage());
        }
        
        if (updateDto.getCategoryIds() != null) {
            Set<NewsCategory> categories = newsCategoryRepository.findAllById(updateDto.getCategoryIds())
                    .stream().collect(Collectors.toSet());
            article.setCategories(categories);
        }

        NewsArticle savedArticle = newsArticleRepository.save(article);
        return convertToDto(savedArticle);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public NewsArticleDto publishArticle(Integer id) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        article.setStatus(NewsArticle.NewsStatus.PUBLISHED);
        article.setPublishDate(LocalDateTime.now());

        NewsArticle savedArticle = newsArticleRepository.save(article);
        return convertToDto(savedArticle);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public NewsArticleDto unpublishArticle(Integer id) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        article.setStatus(NewsArticle.NewsStatus.DRAFT);
        article.setPublishDate(null);

        NewsArticle savedArticle = newsArticleRepository.save(article);
        return convertToDto(savedArticle);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public void deleteArticle(Integer id) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
        
        newsArticleRepository.delete(article);
    }

    public void incrementViews(String slug) {
        newsArticleRepository.findBySlug(slug).ifPresent(article -> {
            article.setViewsCount(article.getViewsCount() + 1);
            newsArticleRepository.save(article);
        });
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String slug = baseSlug;
        int counter = 1;
        
        while (newsArticleRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }

    private String sanitizeContent(String content) {
        return content;
    }

    private NewsArticleDto convertToDto(NewsArticle article) {
        return NewsArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .summary(article.getSummary())
                .content(article.getContent())
                .featuredImage(article.getFeaturedImage())
                .authorName( article.getAuthor().getLastName())
                .status(article.getStatus())
                .publishDate(article.getPublishDate())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .viewsCount(article.getViewsCount())
                .categories(article.getCategories().stream()
                        .map(this::convertCategoryToDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    private NewsArticleSummaryDto convertToSummaryDto(NewsArticle article) {
        return NewsArticleSummaryDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .summary(article.getSummary())
                .featuredImage(article.getFeaturedImage())
                .authorName( article.getAuthor().getLastName())
                .status(article.getStatus())
                .publishDate(article.getPublishDate())
                .viewsCount(article.getViewsCount())
                .categories(article.getCategories().stream()
                        .map(this::convertCategoryToDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    private NewsCategoryDto convertCategoryToDto(NewsCategory category) {
        return NewsCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .color(category.getColor())
                .build();
    }
}