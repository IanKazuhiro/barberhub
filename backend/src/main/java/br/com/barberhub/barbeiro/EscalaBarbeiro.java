package br.com.barberhub.barbeiro;

import java.time.Instant;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Um intervalo de trabalho na semana. O mesmo dia pode ter mais de um (pausa para o almoço). */
@Entity
@Table(name = "escala_barbeiro")
@Getter
@NoArgsConstructor
public class EscalaBarbeiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "barbeiro_id", nullable = false)
    private Long barbeiroId;

    /** 0 = domingo ... 6 = sábado. */
    @Column(name = "dia_semana", nullable = false)
    private short diaSemana;

    @Column(nullable = false)
    private LocalTime inicio;

    @Column(nullable = false)
    private LocalTime fim;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    public EscalaBarbeiro(Long barbeiroId, int diaSemana, LocalTime inicio, LocalTime fim) {
        this.barbeiroId = barbeiroId;
        this.diaSemana = (short) diaSemana;
        this.inicio = inicio;
        this.fim = fim;
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = Instant.now();
    }
}
