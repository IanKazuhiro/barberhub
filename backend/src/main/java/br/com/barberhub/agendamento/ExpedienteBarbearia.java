package br.com.barberhub.agendamento;

import java.time.Instant;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Só leitura por enquanto: os dados vêm da migração V10, e a tela para editar é o UC16. */
@Entity
@Table(name = "expediente_barbearia")
@Getter
@NoArgsConstructor
public class ExpedienteBarbearia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 0 = domingo ... 6 = sábado. */
    @Column(name = "dia_semana", nullable = false)
    private short diaSemana;

    private LocalTime abertura;

    private LocalTime fechamento;

    @Column(nullable = false)
    private boolean fechado;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    ExpedienteBarbearia(int diaSemana, LocalTime abertura, LocalTime fechamento, boolean fechado) {
        this.diaSemana = (short) diaSemana;
        this.abertura = abertura;
        this.fechamento = fechamento;
        this.fechado = fechado;
    }
}
