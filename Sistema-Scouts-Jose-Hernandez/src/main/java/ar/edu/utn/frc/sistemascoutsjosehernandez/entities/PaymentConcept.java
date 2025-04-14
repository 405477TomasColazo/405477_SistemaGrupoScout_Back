package ar.edu.utn.frc.sistemascoutsjosehernandez.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "payment_concepts", schema = "jose_hernandez_db")
public class PaymentConcept {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "default_amount", precision = 10, scale = 2)
    private BigDecimal defaultAmount;

}