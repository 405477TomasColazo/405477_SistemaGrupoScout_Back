package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
}
