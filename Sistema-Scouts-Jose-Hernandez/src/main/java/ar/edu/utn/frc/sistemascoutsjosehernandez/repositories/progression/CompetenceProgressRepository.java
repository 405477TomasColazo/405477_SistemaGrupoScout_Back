package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.CompetenceProgress;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.CompetenceStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.GrowthArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetenceProgressRepository extends JpaRepository<CompetenceProgress, Integer> {
    
    List<CompetenceProgress> findByMarchSheetId(Integer marchSheetId);
    
    List<CompetenceProgress> findByMarchSheetMemberId(Integer memberId);
    
    List<CompetenceProgress> findByStatus(CompetenceStatus status);
    
    @Query("SELECT cp FROM CompetenceProgress cp WHERE cp.marchSheet.member.id = :memberId AND cp.competence.growthArea = :growthArea")
    List<CompetenceProgress> findByMemberIdAndGrowthArea(@Param("memberId") Integer memberId, 
                                                        @Param("growthArea") GrowthArea growthArea);
    
    @Query("SELECT cp FROM CompetenceProgress cp WHERE cp.marchSheet.member.id = :memberId AND cp.status = :status")
    List<CompetenceProgress> findByMemberIdAndStatus(@Param("memberId") Integer memberId, 
                                                    @Param("status") CompetenceStatus status);
    
    @Query("SELECT COUNT(cp) FROM CompetenceProgress cp WHERE cp.marchSheet.member.id = :memberId AND cp.status IN :statuses")
    Long countByMemberIdAndStatusIn(@Param("memberId") Integer memberId, 
                                   @Param("statuses") List<CompetenceStatus> statuses);
    
    @Query("SELECT cp FROM CompetenceProgress cp WHERE cp.status = 'COMPLETED' AND cp.approvedByEducator IS NULL")
    List<CompetenceProgress> findPendingApprovals();
    
    @Query("SELECT cp FROM CompetenceProgress cp JOIN cp.marchSheet.member m JOIN m.section s " +
           "JOIN EducatorsXSection exs ON exs.section.id = s.id WHERE exs.educator.id = :educatorId " +
           "AND cp.status = 'COMPLETED' AND cp.approvedByEducator IS NULL")
    List<CompetenceProgress> findPendingApprovalsByEducator(@Param("educatorId") Integer educatorId);
    
    boolean existsByMarchSheetIdAndCompetenceId(Integer marchSheetId, Integer competenceId);
}