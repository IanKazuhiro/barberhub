package br.com.barberhub.barbeiro;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FolgaRequisicao(
        @NotNull(message = "Informe a data da folga.")
        LocalDate data,

        @Size(max = 200, message = "O motivo deve ter no máximo 200 caracteres.")
        String motivo) {
}
