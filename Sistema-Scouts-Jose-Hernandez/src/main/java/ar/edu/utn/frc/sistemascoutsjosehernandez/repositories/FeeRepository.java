package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Fee;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Integer> {
    List<Fee> findByMemberIdAndStatus(Integer memberId, PaymentStatus status);

    @Modifying
    @Query("UPDATE Fee f SET f.status = :status WHERE f.id IN :ids")
    void updateStatusByIds(List<Integer> ids, String status);
    
    // Monthly fee management queries
    Optional<Fee> findByMemberIdAndPeriod(Integer memberId, String period);
    
    List<Fee> findByPeriodAndStatus(String period, PaymentStatus status);
    
    @Query("SELECT f FROM Fee f WHERE f.member.id = :memberId AND f.period = :period")
    Optional<Fee> findByMemberAndPeriod(@Param("memberId") Integer memberId, @Param("period") String period);
    
    @Query("SELECT f FROM Fee f WHERE f.period = :period AND f.status = 'PENDING'")
    List<Fee> findPendingFeesByPeriod(@Param("period") String period);
    
    @Query("SELECT f FROM Fee f WHERE f.member.section.id = :sectionId AND f.period = :period")
    List<Fee> findByMemberSectionAndPeriod(@Param("sectionId") Integer sectionId, @Param("period") String period);
    
    @Query("SELECT f FROM Fee f WHERE f.member.memberType.description = 'Protagonista' AND f.status = 'PENDING'")
    List<Fee> findAllPendingProtagonistFees();
    
    @Modifying
    @Query("UPDATE Fee f SET f.amount = :newAmount WHERE f.period = :period AND f.status = 'PENDING'")
    int updatePendingFeesAmountForPeriod(@Param("period") String period, @Param("newAmount") BigDecimal newAmount);
    
    @Modifying
    @Query("UPDATE Fee f SET f.amount = :newAmount WHERE f.period <= :currentPeriod AND f.status = 'PENDING' AND f.member.memberType.description = 'Protagonista'")
    int updateAllPendingProtagonistFeesAmount(@Param("currentPeriod") String currentPeriod, @Param("newAmount") BigDecimal newAmount);
    
    @Query("SELECT COUNT(f) FROM Fee f WHERE f.period = :period AND f.member.memberType.description = 'Protagonista'")
    long countProtagonistFeesByPeriod(@Param("period") String period);
    
    @Query("SELECT f FROM Fee f WHERE f.member.memberType.description = 'Protagonista' AND f.member.isActive = true AND f.status = 'PENDING' ORDER BY f.period ASC")
    List<Fee> findAllActiveProtagonistPendingFees();
}
