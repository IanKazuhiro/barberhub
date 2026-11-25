package br.com.barberhub.barbeiro;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Um intervalo da escala, na ida e na volta da API. Dia 0 = domingo ... 6 = sábado. */
public record IntervaloEscala(
        @NotNull(message = "Informe o dia da semana.")
        @Min(value = 0, message = "O dia da semana vai de 0 (domingo) a 6 (sábado).")
        @Max(value = 6, message = "O dia da semana vai de 0 (domingo) a 6 (sábado).")
        Integer diaSemana,

        @NotNull(message = "Informe o início.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime inicio,

        @NotNull(message = "Informe o fim.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime fim) {

    static IntervaloEscala de(EscalaBarbeiro e) {
        return new IntervaloEscala((int) e.getDiaSemana(), e.getInicio(), e.getFim());
    }
}
