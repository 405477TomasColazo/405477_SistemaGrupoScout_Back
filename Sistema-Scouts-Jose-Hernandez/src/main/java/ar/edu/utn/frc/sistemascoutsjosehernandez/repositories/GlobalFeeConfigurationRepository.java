package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.GlobalFeeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalFeeConfigurationRepository extends JpaRepository<GlobalFeeConfiguration, Integer> {
    
    /**
     * Find the current active global fee configuration
     */
    @Query("SELECT g FROM GlobalFeeConfiguration g WHERE g.isActive = true ORDER BY g.updatedAt DESC")
    Optional<GlobalFeeConfiguration> findActiveConfiguration();
    
    /**
     * Check if there's any active configuration
     */
    boolean existsByIsActiveTrue();
}