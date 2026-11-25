package br.com.barberhub.agendamento;

import java.util.List;

/** Próximos em ordem de chegada; anteriores do mais recente para o mais antigo. */
public record MeusAgendamentosResposta(List<AgendamentoResposta> proximos, List<AgendamentoResposta> anteriores) {
}
