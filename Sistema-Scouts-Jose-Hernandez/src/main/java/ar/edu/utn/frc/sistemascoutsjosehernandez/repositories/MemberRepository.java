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
}
