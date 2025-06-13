package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Educator;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "competence_progress")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenceProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "march_sheet_id", nullable = false)
    private MarchSheet marchSheet;

    @Lob
    @Column(name = "own_action")
    private String ownAction;

    @Lob
    @Column(name = "achievement")
    private String achievement;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("'PENDING'")
    private CompetenceStatus status = CompetenceStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_educator_id")
    private Educator approvedByEducator;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Lob
    @Column(name = "educator_comments")
    private String educatorComments;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}