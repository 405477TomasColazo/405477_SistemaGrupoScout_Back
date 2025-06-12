package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Integer> {
    
    Optional<NewsArticle> findBySlug(String slug);
    
    Page<NewsArticle> findByStatusOrderByPublishDateDesc(NewsArticle.NewsStatus status, Pageable pageable);
    
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' ORDER BY a.publishDate DESC")
    List<NewsArticle> findLatestPublishedArticles(Pageable pageable);
    
    @Query("SELECT a FROM NewsArticle a JOIN a.categories c WHERE c.id = :categoryId AND a.status = 'PUBLISHED' ORDER BY a.publishDate DESC")
    Page<NewsArticle> findByCategoryIdAndStatus(@Param("categoryId") Integer categoryId, Pageable pageable);
    
    @Query("SELECT a FROM NewsArticle a WHERE a.status = 'PUBLISHED' AND " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.publishDate DESC")
    Page<NewsArticle> searchPublishedArticles(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    boolean existsBySlug(String slug);
}