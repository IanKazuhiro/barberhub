package br.com.barberhub.agendamento;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

public record NovoAgendamentoRequisicao(
        @NotNull(message = "Escolha o serviço.")
        Long servicoId,

        @NotNull(message = "Escolha o barbeiro.")
        Long barbeiroId,

        /** Data e hora no fuso da barbearia, ex.: 2026-11-25T10:00. */
        @NotNull(message = "Escolha o horário.")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime inicio) {
}
