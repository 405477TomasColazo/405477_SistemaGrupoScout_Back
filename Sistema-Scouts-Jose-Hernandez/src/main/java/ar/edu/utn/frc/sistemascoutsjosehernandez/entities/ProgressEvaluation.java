package ar.edu.utn.frc.sistemascoutsjosehernandez.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "progress_evaluations")
public class ProgressEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "educator_id", nullable = false)
    private Educator educator;

    @NotNull
    @Column(name = "evaluation_date", nullable = false)
    private LocalDate evaluationDate;

    @Size(max = 100)
    @NotNull
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @NotNull
    @Column(name = "achievement_level", nullable = false)
    private Integer achievementLevel;

    @Lob
    @Column(name = "comments")
    private String comments;

}