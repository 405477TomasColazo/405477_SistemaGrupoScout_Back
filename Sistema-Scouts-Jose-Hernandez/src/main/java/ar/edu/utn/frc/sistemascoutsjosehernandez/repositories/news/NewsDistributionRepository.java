package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsDistributionRepository extends JpaRepository<NewsDistribution, Integer> {
    
    List<NewsDistribution> findByArticleIdOrderBySentAtDesc(Integer articleId);
    
    List<NewsDistribution> findByStatusOrderBySentAtDesc(NewsDistribution.DistributionStatus status);
}