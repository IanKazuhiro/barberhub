package br.com.barberhub.barbeiro;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A escala inteira da semana: o que não vier deixa de existir. Lista vazia limpa a escala. */
public record EscalaRequisicao(
        @NotNull(message = "Informe os intervalos da escala.")
        @Size(max = 28, message = "A escala aceita no máximo 28 intervalos na semana.")
        @Valid
        List<@NotNull(message = "Intervalo vazio.") IntervaloEscala> intervalos) {
}
