package ar.edu.utn.frc.sistemascoutsjosehernandez.entities.accounting;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "accounting_entries")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountingEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "entry_number", unique = true)
    private Integer entryNumber;

    @NotNull
    @Column(name = "entry_date")
    private LocalDate entryDate;

    @NotNull
    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "origin_type")
    private OriginType originType;

    @Size(max = 100)
    @Column(name = "origin_reference")
    private String originReference; // Payment ID if automatic

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EntryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_user_id")
    private User modifiedByUser;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "accountingEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountingMovement> movements;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}