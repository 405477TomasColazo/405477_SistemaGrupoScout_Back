package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.Competence;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.GrowthArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Integer> {
    
    List<Competence> findByGrowthArea(GrowthArea growthArea);
    
    @Query("SELECT c FROM Competence c WHERE c.section IS NULL OR c.section.id = :sectionId")
    List<Competence> findBySectionIdOrGeneral(@Param("sectionId") Integer sectionId);
    
    @Query("SELECT c FROM Competence c WHERE " +
           "(c.section IS NULL OR c.section.id = :sectionId) " +
           "AND (:growthArea IS NULL OR c.growthArea = :growthArea)")
    List<Competence> findBySectionAndGrowthArea(@Param("sectionId") Integer sectionId, 
                                               @Param("growthArea") GrowthArea growthArea);
    
    List<Competence> findBySectionIsNull();
}