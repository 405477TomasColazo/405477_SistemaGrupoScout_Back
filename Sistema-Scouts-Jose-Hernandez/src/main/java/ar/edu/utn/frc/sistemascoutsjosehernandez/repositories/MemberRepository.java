package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    List<Member> findAllBySection(Section section);

    List<Member> findAllByIsTutor(boolean isTutor);
    
    Optional<Member> findByUser(User user);

    List<Member> findAllByMemberType_Id(Integer memberTypeId);

    List<Member> findAllByFamilyGroup_Id(Integer familyGroupId);

    // Soft delete queries
    List<Member> findAllByIsActiveTrueAndIsTutor(boolean isTutor);
    
    List<Member> findAllByIsActiveTrueAndSection(Section section);
    
    Optional<Member> findByDniAndFamilyGroup_UserAndIsActiveTrue(String dni, User user);
    
    Optional<Member> findByDniAndFamilyGroup_User(String dni, User user);
    
    List<Member> findAllByIsActiveTrueAndFamilyGroup_Id(Integer familyGroupId);
}
