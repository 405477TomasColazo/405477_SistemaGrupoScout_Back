package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "news_categories", schema = "jose_hernandez_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 7)
    private String color;

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<NewsArticle> articles = new HashSet<>();
}