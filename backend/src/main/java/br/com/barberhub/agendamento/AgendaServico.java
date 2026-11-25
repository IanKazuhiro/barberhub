package br.com.barberhub.agendamento;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Agenda do dia do próprio barbeiro (RF-AGE-07 e RN04). */
@Service
@RequiredArgsConstructor
public class AgendaServico {

    private final AgendamentoRepositorio agendamentos;
    private final DisponibilidadeServico disponibilidade;
    private final Clock relogio;

    /** Sem data, é o dia de hoje no fuso da barbearia. */
    @Transactional(readOnly = true)
    public List<ItemAgendaResposta> doDia(Long barbeiroId, LocalDate data) {
        LocalDate dia = data == null ? LocalDate.now(relogio) : data;
        return agendamentos.agendaDoBarbeiro(barbeiroId, disponibilidade.instante(dia.atStartOfDay()),
                        disponibilidade.instante(dia.plusDays(1).atStartOfDay())).stream()
                .map(this::paraResposta)
                .toList();
    }

    private ItemAgendaResposta paraResposta(AgendamentoComCliente linha) {
        Agendamento a = linha.agendamento();
        return new ItemAgendaResposta(a.getId(), disponibilidade.local(a.getInicio()).toLocalTime(),
                disponibilidade.local(a.getFim()).toLocalTime(), a.getEstado(), a.getClienteId(), linha.cliente(),
                linha.telefone(), linha.servico(), linha.tipoCorte(), linha.observacoes());
    }
}
