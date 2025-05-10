package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Educator;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducatorRepository extends JpaRepository<Educator, Integer> {
    Optional<Educator> findByUser_Id(Integer id);

    List<Educator> findAllBySection(Section section);
}
