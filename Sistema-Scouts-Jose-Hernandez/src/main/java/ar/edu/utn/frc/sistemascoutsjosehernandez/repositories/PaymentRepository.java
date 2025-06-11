package ar.edu.utn.frc.sistemascoutsjosehernandez.repositories;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("SELECT p FROM Payment p WHERE " +
            "(:memberId IS NULL OR p.memberId = :memberId) AND " +
            "(:dateFrom IS NULL OR p.paymentDate >= :dateFrom) AND " +
            "(:dateTo IS NULL OR p.paymentDate <= :dateTo) AND " +
            "(:minAmount IS NULL OR p.amount >= :minAmount)")
    Page<Payment> findByFilters(Integer memberId, String dateFrom, String dateTo, BigDecimal minAmount, Pageable pageable);
}
