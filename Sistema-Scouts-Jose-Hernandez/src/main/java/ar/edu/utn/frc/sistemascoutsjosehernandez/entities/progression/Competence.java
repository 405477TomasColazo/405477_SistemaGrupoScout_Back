package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "competences", schema = "jose_hernandez_db")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Competence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "growth_area", nullable = false)
    private GrowthArea growthArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToMany(mappedBy = "competence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SuggestedAction> suggestedActions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "competence_guiding_questions", joinColumns = @JoinColumn(name = "competence_id"))
    @Column(name = "question")
    private List<String> guidingQuestions = new ArrayList<>();
}