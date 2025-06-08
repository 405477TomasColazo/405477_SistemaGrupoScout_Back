package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations",schema = "jose_hernandez_db" )
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "member_name")
    private String memberName;

    @Column(name = "member_last_name")
    private String memberLastName;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
    }
}
