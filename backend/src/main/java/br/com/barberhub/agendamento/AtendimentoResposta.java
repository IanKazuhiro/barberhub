package br.com.barberhub.agendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Um atendimento concluído, no histórico do cliente. */
public record AtendimentoResposta(Long id,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime inicio,
        String servico, String barbeiro, BigDecimal valor) {
}
