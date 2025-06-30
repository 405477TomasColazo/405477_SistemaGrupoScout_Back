package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsImageRepository extends JpaRepository<NewsImage, Integer> {
    
    List<NewsImage> findByArticleIdOrderByCreatedAtAsc(Integer articleId);
    
    void deleteByArticleId(Integer articleId);
}