package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "events", schema = "jose_hernandez_db")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String location;

    @ElementCollection
    @CollectionTable(name = "event_sections",
            joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "section")
    private Set<String> sections = new HashSet<>();

    private BigDecimal cost;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "current_capacity")
    private Integer currentCapacity = 0;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_type", nullable = false)
    private InvitationType invitationType = InvitationType.ALL;

    @ElementCollection
    @CollectionTable(name = "event_invited_members",
            joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "member_id")
    private Set<Integer> invitedMembers = new HashSet<>();

    @Column(name = "requires_payment")
    private Boolean requiresPayment = false;

    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventAttachment> attachments = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
