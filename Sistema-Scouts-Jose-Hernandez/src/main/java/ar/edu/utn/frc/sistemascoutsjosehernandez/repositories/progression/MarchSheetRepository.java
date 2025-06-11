package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.MarchSheet;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.ProgressionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarchSheetRepository extends JpaRepository<MarchSheet, Integer> {
    
    Optional<MarchSheet> findByMemberId(Integer memberId);
    
    List<MarchSheet> findByProgressionStage(ProgressionStage progressionStage);
    
    @Query("SELECT ms FROM MarchSheet ms JOIN ms.member m JOIN m.section s WHERE s.id = :sectionId")
    List<MarchSheet> findByMemberSectionId(@Param("sectionId") Integer sectionId);
    
    @Query("SELECT ms FROM MarchSheet ms WHERE ms.member.id IN :memberIds")
    List<MarchSheet> findByMemberIds(@Param("memberIds") List<Integer> memberIds);
    
    boolean existsByMemberId(Integer memberId);
}