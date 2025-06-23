package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {
    Section findFirstByDescription(@Size(max = 50) @NotNull String description);
    
    Optional<Section> findByDescription(String description);
    
    @Query("SELECT s FROM Section s WHERE :age >= s.minAge AND :age <= s.maxAge")
    Optional<Section> findByAgeRange(@Param("age") Integer age);
}
