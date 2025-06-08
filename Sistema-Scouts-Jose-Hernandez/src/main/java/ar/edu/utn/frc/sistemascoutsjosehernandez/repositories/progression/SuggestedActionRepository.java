package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.SuggestedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestedActionRepository extends JpaRepository<SuggestedAction, Integer> {
    
    List<SuggestedAction> findByCompetenceId(Integer competenceId);
    
    void deleteByCompetenceId(Integer competenceId);
}