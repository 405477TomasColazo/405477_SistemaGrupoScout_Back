package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Integer> {
    
    Optional<NewsCategory> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    boolean existsByName(String name);
}