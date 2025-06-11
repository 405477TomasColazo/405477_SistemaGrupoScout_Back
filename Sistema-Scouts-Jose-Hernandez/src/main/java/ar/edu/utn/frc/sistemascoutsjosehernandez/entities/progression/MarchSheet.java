package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "march_sheets", schema = "jose_hernandez_db")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarchSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Size(max = 100)
    @Column(name = "totem", length = 100)
    private String totem;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "progression_stage", nullable = false)
    private ProgressionStage progressionStage;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "marchSheet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompetenceProgress> competenceProgress = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}