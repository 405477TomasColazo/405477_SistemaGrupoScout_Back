package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "news_distributions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_by", nullable = false)
    private User sentBy;

    @Column(name = "total_recipients")
    private int totalRecipients;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DistributionStatus status = DistributionStatus.PENDING;

    @OneToMany(mappedBy = "distribution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<DistributionFilter> filters = new HashSet<>();

    public enum DistributionStatus {
        PENDING,
        SENDING,
        SENT,
        FAILED
    }
}