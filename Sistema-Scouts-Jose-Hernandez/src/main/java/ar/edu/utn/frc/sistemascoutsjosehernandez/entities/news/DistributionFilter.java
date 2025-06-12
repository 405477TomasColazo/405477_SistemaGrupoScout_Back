package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "distribution_filters", schema = "jose_hernandez_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribution_id", nullable = false)
    private NewsDistribution distribution;

    @Enumerated(EnumType.STRING)
    @Column(name = "filter_type", nullable = false)
    private FilterType filterType;

    @Column(name = "filter_value")
    private String filterValue;

    public enum FilterType {
        SECTION,
        MEMBER_TYPE,
        FAMILY_GROUP,
        ALL
    }
}