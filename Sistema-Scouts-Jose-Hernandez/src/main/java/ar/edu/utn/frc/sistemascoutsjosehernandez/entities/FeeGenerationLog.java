package ar.edu.utn.frc.sistemascoutsjosehernandez.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fee_generation_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeeGenerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "generation_month", nullable = false, length = 7) // Format: YYYY-MM
    @JsonProperty("targetMonth")
    private String generationMonth;

    @NotNull
    @Column(name = "execution_date", nullable = false)
    @JsonProperty("executedAt")
    private LocalDateTime executionDate;

    @NotNull
    @Column(name = "total_members_processed", nullable = false)
    private Integer totalMembersProcessed;

    @NotNull
    @Column(name = "total_fees_generated", nullable = false)
    private Integer totalFeesGenerated;

    @NotNull
    @Column(name = "total_fees_updated", nullable = false)
    private Integer totalFeesUpdated;

    @Column(name = "execution_type", length = 20) // AUTOMATIC, MANUAL
    @JsonProperty("generationType")
    private String executionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by_user_id")
    private User executedByUser;

    @Lob
    @Column(name = "execution_details")
    @JsonProperty("details")
    private String executionDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FeeGenerationStatus status;

    public enum FeeGenerationStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED
    }

    @PrePersist
    protected void onCreate() {
        if (executionDate == null) {
            executionDate = LocalDateTime.now();
        }
    }
}