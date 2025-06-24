package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Payment;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("SELECT p FROM Payment p WHERE " +
            "(:memberId IS NULL OR p.memberId = :memberId) AND " +
            "(:dateFrom IS NULL OR p.paymentDate >= :dateFrom) AND " +
            "(:dateTo IS NULL OR p.paymentDate <= :dateTo) AND " +
            "(:minAmount IS NULL OR p.amount >= :minAmount)")
    Page<Payment> findByFilters(Integer memberId, String dateFrom, String dateTo, BigDecimal minAmount, Pageable pageable);
    
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN Member m ON p.memberId = m.id " +
            "LEFT JOIN FamilyGroup fg ON m.familyGroup.id = fg.id " +
            "LEFT JOIN Section s ON m.section.id = s.id " +
            "WHERE (:memberId IS NULL OR p.memberId = :memberId) " +
            "AND (:familyGroupId IS NULL OR fg.id = :familyGroupId) " +
            "AND (:sectionId IS NULL OR s.id = :sectionId) " +
            "AND (:dateFrom IS NULL OR p.paymentDate >= :dateFrom) " +
            "AND (:dateTo IS NULL OR p.paymentDate <= :dateTo) " +
            "AND (:minAmount IS NULL OR p.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR p.amount <= :maxAmount) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:paymentMethod IS NULL OR p.paymentMethod LIKE %:paymentMethod%) " +
            "AND (:memberName IS NULL OR CONCAT(m.name, ' ', m.lastname) LIKE %:memberName%)")
    Page<Payment> findByAdminFilters(
            @Param("memberId") Integer memberId,
            @Param("familyGroupId") Integer familyGroupId,
            @Param("sectionId") Integer sectionId,
            @Param("dateFrom") String dateFrom,
            @Param("dateTo") String dateTo,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("status") PaymentStatus status,
            @Param("paymentMethod") String paymentMethod,
            @Param("memberName") String memberName,
            Pageable pageable);

    @Query("SELECT COUNT(p), " +
            "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.status = 'PENDING' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.status = 'FAILED' THEN 1 ELSE 0 END), " +
            "SUM(p.amount), " +
            "SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END), " +
            "SUM(CASE WHEN p.status = 'PENDING' THEN p.amount ELSE 0 END), " +
            "AVG(p.amount) " +
            "FROM Payment p " +
            "WHERE (:dateFrom IS NULL OR p.paymentDate >= :dateFrom) " +
            "AND (:dateTo IS NULL OR p.paymentDate <= :dateTo)")
    List<Object[]> getPaymentStatistics(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo);

    @Query("SELECT s.id, s.description, COUNT(f), SUM(f.amount), COUNT(DISTINCT m.id) " +
            "FROM Fee f " +
            "JOIN Member m ON f.member.id = m.id " +
            "JOIN Section s ON m.section.id = s.id " +
            "WHERE f.status = 'PENDING' " +
            "GROUP BY s.id, s.description")
    List<Object[]> getPendingPaymentsBySection();
    
    Optional<Payment> findByReferenceId(String referenceId);
}
