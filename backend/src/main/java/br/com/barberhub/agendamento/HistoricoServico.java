package br.com.barberhub.agendamento;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.acesso.ClienteRepositorio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/** Histórico de atendimentos concluídos de um cliente (RF-CLI-03). */
@Service
@RequiredArgsConstructor
public class HistoricoServico {

    static final String CLIENTE_NAO_ENCONTRADO = "Cliente não encontrado.";

    private final ClienteRepositorio clientes;
    private final AgendamentoRepositorio agendamentos;
    private final DisponibilidadeServico disponibilidade;

    @Transactional(readOnly = true)
    public List<AtendimentoResposta> doCliente(Long clienteId) {
        if (!clientes.existsById(clienteId)) {
            throw new RecursoNaoEncontradoException(CLIENTE_NAO_ENCONTRADO);
        }
        return agendamentos.doClientePorEstado(clienteId, EstadoAgendamento.CONCLUIDO).stream()
                .map(d -> new AtendimentoResposta(d.agendamento().getId(),
                        disponibilidade.local(d.agendamento().getInicio()), d.servico(), d.barbeiro(),
                        d.agendamento().getPrecoCobrado()))
                .toList();
    }
}
