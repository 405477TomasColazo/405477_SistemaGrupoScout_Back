package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Fee;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    @Query("SELECT f FROM Fee f " +
           "WHERE f.status = 'PENDING' " +
           "AND (:memberName IS NULL OR :memberName = '' OR " +
           "     LOWER(CONCAT(f.member.name, ' ', f.member.lastname)) LIKE LOWER(CONCAT('%', :memberName, '%'))) " +
           "AND (:sectionId IS NULL OR f.member.section.id = :sectionId) " +
           "AND (:familyGroupId IS NULL OR f.member.familyGroup.id = :familyGroupId) " +
           "AND (:minAmount IS NULL OR f.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR f.amount <= :maxAmount) " +
           "AND (:period IS NULL OR :period = '' OR f.period = :period) " +
           "ORDER BY f.period DESC, f.member.name ASC")
    Page<Fee> findPendingFeesForAdmin(
            @Param("memberName") String memberName,
            @Param("sectionId") Integer sectionId,
            @Param("familyGroupId") Integer familyGroupId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("period") String period,
            Pageable pageable);
}
