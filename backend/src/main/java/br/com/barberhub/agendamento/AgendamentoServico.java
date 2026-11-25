package br.com.barberhub.agendamento;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.catalogo.Servico;
import br.com.barberhub.compartilhado.erro.ConflitoException;
import lombok.RequiredArgsConstructor;

/** Agendamento feito pelo cliente (RF-AGE-01 e RF-AGE-03). */
@Service
@RequiredArgsConstructor
public class AgendamentoServico {

    private final AgendamentoRepositorio agendamentos;
    private final DisponibilidadeServico disponibilidade;
    private final PrazoServico prazo;

    @Transactional
    public AgendamentoResposta agendar(Long clienteId, NovoAgendamentoRequisicao requisicao) {
        Servico servico = disponibilidade.servicoAtivo(requisicao.servicoId());
        Usuario barbeiro = disponibilidade.exigirBarbeiroAtivo(requisicao.barbeiroId());
        LocalDateTime inicio = requisicao.inicio();
        disponibilidade.exigirLivre(barbeiro.getId(), servico.getDuracaoMinutos(), inicio, null);

        Agendamento agendamento = new Agendamento();
        agendamento.setClienteId(clienteId);
        agendamento.setBarbeiroId(barbeiro.getId());
        agendamento.setServicoId(servico.getId());
        agendamento.setInicio(disponibilidade.instante(inicio));
        agendamento.setFim(disponibilidade.instante(inicio.plusMinutes(servico.getDuracaoMinutos())));
        agendamento.setEstado(EstadoAgendamento.AGENDADO);
        agendamento.setPrecoCobrado(servico.getPreco());
        agendamento.setOrigem(OrigemAgendamento.APP);
        agendamento.setCriadoPor(clienteId);
        agendamento = gravar(agendamento);

        return new AgendamentoResposta(agendamento.getId(), inicio, disponibilidade.local(agendamento.getFim()),
                agendamento.getEstado(), servico.getId(), servico.getNome(), barbeiro.getId(), barbeiro.getNome(),
                agendamento.getPrecoCobrado(), prazo.podeAlterar(agendamento, prazo.horasDeAntecedencia()));
    }

    /**
     * Dois pedidos ao mesmo tempo passam pela conferência acima; quem decide é a restrição do banco (RN01).
     * O perdedor pode sair como violação ou como deadlock entre as duas inserções: para o cliente é o mesmo aviso.
     */
    private Agendamento gravar(Agendamento agendamento) {
        try {
            return agendamentos.saveAndFlush(agendamento);
        } catch (DataIntegrityViolationException | PessimisticLockingFailureException e) {
            throw new ConflitoException(DisponibilidadeServico.HORARIO_OCUPADO);
        }
    }
}
