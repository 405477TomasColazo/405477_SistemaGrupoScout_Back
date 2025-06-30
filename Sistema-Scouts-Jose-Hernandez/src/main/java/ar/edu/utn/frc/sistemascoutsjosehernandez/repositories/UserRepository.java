package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPasswordResetToken(String passwordResetToken);
    
    @Query("SELECT u FROM User u JOIN u.rolesXUser rx JOIN rx.role r WHERE r.description = :roleDescription")
    List<User> findByRoleDescription(@Param("roleDescription") String roleDescription);
}
