package ar.edu.utn.frc.sistemascoutsjosehernandez.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "family_groups")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FamilyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_contact_id")
    private Member mainContact;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "familyGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Member> members;

    @Transient
    public List<Member> getTutors() {
        List<Member> m = members.stream()
                .filter(Member::getIsTutor)
                .filter(Member::getIsActive)
                .collect(Collectors.toList());
        m.remove(mainContact);
        return m;
    }

    @Transient
    public List<Member> getMemberProtagonists() {
        return members.stream()
                .filter(m -> !m.getIsTutor())
                .filter(Member::getIsActive)
                .collect(Collectors.toList());
    }

}
