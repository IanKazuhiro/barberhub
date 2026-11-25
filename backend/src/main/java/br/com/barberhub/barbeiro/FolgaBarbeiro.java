package br.com.barberhub.barbeiro;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "folga_barbeiro")
@Getter
@NoArgsConstructor
public class FolgaBarbeiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "barbeiro_id", nullable = false)
    private Long barbeiroId;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 200)
    private String motivo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    public FolgaBarbeiro(Long barbeiroId, LocalDate data, String motivo) {
        this.barbeiroId = barbeiroId;
        this.data = data;
        this.motivo = motivo;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = Instant.now();
    }
}
