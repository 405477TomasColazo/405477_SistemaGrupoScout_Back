package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.FamilyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyGroupRepository extends JpaRepository<FamilyGroup, Integer> {
    FamilyGroup findFamilyGroupsByUser_Id(Integer userId);
}
