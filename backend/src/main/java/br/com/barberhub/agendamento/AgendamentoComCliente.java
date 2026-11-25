package br.com.barberhub.agendamento;

/** Um agendamento com os nomes e as preferências do cliente, vindo de uma consulta só. */
public record AgendamentoComCliente(Agendamento agendamento, String cliente, String telefone, String tipoCorte,
        String observacoes, String servico) {
}
