package br.com.barberhub.agendamento;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Agendamentos do próprio cliente (RF-AGE-04). */
@Service
@RequiredArgsConstructor
public class MeusAgendamentosServico {

    private static final Comparator<AgendamentoDetalhado> POR_INICIO =
            Comparator.comparing(d -> d.agendamento().getInicio());

    private final AgendamentoRepositorio agendamentos;
    private final DisponibilidadeServico disponibilidade;
    private final PrazoServico prazo;
    private final Clock relogio;

    @Transactional(readOnly = true)
    public MeusAgendamentosResposta listar(Long clienteId) {
        Instant agora = relogio.instant();
        int horas = prazo.horasDeAntecedencia();
        Map<Boolean, List<AgendamentoDetalhado>> grupos = agendamentos.doCliente(clienteId).stream()
                .collect(Collectors.partitioningBy(d -> ehProximo(d.agendamento(), agora)));
        return new MeusAgendamentosResposta(
                grupos.get(true).stream().sorted(POR_INICIO).map(d -> paraResposta(d, horas)).toList(),
                grupos.get(false).stream().sorted(POR_INICIO.reversed()).map(d -> paraResposta(d, horas)).toList(),
                horas);
    }

    /** Cancelado sai dos próximos mesmo com data futura; em andamento ainda conta como próximo. */
    private static boolean ehProximo(Agendamento a, Instant agora) {
        return a.getEstado() == EstadoAgendamento.AGENDADO && a.getFim().isAfter(agora);
    }

    private AgendamentoResposta paraResposta(AgendamentoDetalhado d, int horas) {
        Agendamento a = d.agendamento();
        return new AgendamentoResposta(a.getId(), disponibilidade.local(a.getInicio()),
                disponibilidade.local(a.getFim()), a.getEstado(), a.getServicoId(), d.servico(),
                a.getBarbeiroId(), d.barbeiro(), a.getPrecoCobrado(), prazo.podeAlterar(a, horas));
    }
}
