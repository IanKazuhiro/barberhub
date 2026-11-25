package br.com.barberhub.agendamento;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

/** O serviço e o barbeiro continuam os mesmos: só muda o horário. */
public record RemarcarRequisicao(
        @NotNull(message = "Escolha o novo horário.")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime inicio) {
}
