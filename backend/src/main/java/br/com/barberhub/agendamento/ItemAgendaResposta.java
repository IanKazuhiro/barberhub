package br.com.barberhub.agendamento;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/** Uma linha da agenda do barbeiro. As observações do cliente ficam à mão na hora de atender (RF-CLI-04). */
public record ItemAgendaResposta(Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime inicio,
        @JsonFormat(pattern = "HH:mm") LocalTime fim,
        EstadoAgendamento estado, Long clienteId, String cliente, String telefone, String servico,
        String tipoCorte, String observacoes) {
}
