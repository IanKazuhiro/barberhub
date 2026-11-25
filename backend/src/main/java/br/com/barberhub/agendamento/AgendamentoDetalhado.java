package br.com.barberhub.agendamento;

/** Um agendamento já com os nomes, vindo de uma consulta só (sem uma busca por linha). */
public record AgendamentoDetalhado(Agendamento agendamento, String servico, String barbeiro) {
}
