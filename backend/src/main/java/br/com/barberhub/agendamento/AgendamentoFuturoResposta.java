package br.com.barberhub.agendamento;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Um agendamento em aberto de um barbeiro, com o contato do cliente para o administrador resolver. */
public record AgendamentoFuturoResposta(Long id,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime inicio,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fim,
        String cliente, String telefone, String servico) {
}
