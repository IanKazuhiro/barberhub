package br.com.barberhub.agendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Início e fim vão no fuso da barbearia, para a tela mostrar a mesma hora em qualquer aparelho. */
public record AgendamentoResposta(Long id,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime inicio,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fim,
        EstadoAgendamento estado, Long servicoId, String servico, Long barbeiroId, String barbeiro,
        BigDecimal precoCobrado, boolean podeAlterar) {
}
