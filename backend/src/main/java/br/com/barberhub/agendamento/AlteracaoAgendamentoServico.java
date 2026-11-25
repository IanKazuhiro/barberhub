package br.com.barberhub.agendamento;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ConflitoException;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/** Cancelar e remarcar pelo cliente (RF-AGE-05 e RF-AGE-06). */
@Service
@RequiredArgsConstructor
public class AlteracaoAgendamentoServico {

    static final String NAO_ENCONTRADO = "Agendamento não encontrado.";
    static final String NAO_ESTA_AGENDADO = "Este agendamento não está mais em aberto.";
    static final String FORA_DO_PRAZO =
            "Não é possível cancelar ou remarcar: o prazo é de %d hora(s) antes do horário marcado.";
    static final String MESMO_HORARIO = "Escolha um horário diferente do atual.";

    private final AgendamentoRepositorio agendamentos;
    private final DisponibilidadeServico disponibilidade;
    private final PrazoServico prazo;
    private final AuditoriaServico auditoria;
    private final Clock relogio;

    @Transactional
    public void cancelar(Long clienteId, Long id) {
        Agendamento agendamento = buscarAlteravel(clienteId, id);
        agendamento.setEstado(EstadoAgendamento.CANCELADO);
        agendamento.setCanceladoEm(relogio.instant());
        agendamentos.save(agendamento);
        auditoria.registrar(clienteId, "CANCELAR_AGENDAMENTO", "agendamento", id);
    }

    /** Muda a mesma linha: o horário antigo é liberado e o novo é reservado na mesma operação. */
    @Transactional
    public void remarcar(Long clienteId, Long id, RemarcarRequisicao requisicao) {
        Agendamento agendamento = buscarAlteravel(clienteId, id);
        Instant novoInicio = disponibilidade.instante(requisicao.inicio());
        if (novoInicio.equals(agendamento.getInicio())) {
            throw new ExcecaoNegocio(MESMO_HORARIO);
        }
        disponibilidade.exigirBarbeiroAtivo(agendamento.getBarbeiroId());
        int duracao = duracaoDe(agendamento);
        disponibilidade.exigirLivre(agendamento.getBarbeiroId(), duracao, requisicao.inicio(), id);

        agendamento.setInicio(novoInicio);
        agendamento.setFim(novoInicio.plus(Duration.ofMinutes(duracao)));
        try {
            agendamentos.saveAndFlush(agendamento);
        } catch (DataIntegrityViolationException | PessimisticLockingFailureException e) {
            throw new ConflitoException(DisponibilidadeServico.HORARIO_OCUPADO);
        }
        auditoria.registrar(clienteId, "REMARCAR_AGENDAMENTO", "agendamento", id);
    }

    @Transactional(readOnly = true)
    public List<LocalTime> horariosParaRemarcar(Long clienteId, Long id, LocalDate data) {
        Agendamento agendamento = agendamentos.findByIdAndClienteId(id, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));
        disponibilidade.exigirBarbeiroAtivo(agendamento.getBarbeiroId());
        LocalTime atual = disponibilidade.local(agendamento.getInicio()).toLocalTime();
        boolean mesmoDia = disponibilidade.local(agendamento.getInicio()).toLocalDate().equals(data);
        return disponibilidade.horariosLivres(agendamento.getBarbeiroId(), duracaoDe(agendamento), data, id).stream()
                .filter(h -> !(mesmoDia && h.equals(atual)))
                .toList();
    }

    /** Trava a linha: dois cliques ao mesmo tempo (cancelar e remarcar) são decididos um de cada vez. */
    private Agendamento buscarAlteravel(Long clienteId, Long id) {
        Agendamento agendamento = agendamentos.travarDoCliente(id, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));
        if (agendamento.getEstado() != EstadoAgendamento.AGENDADO) {
            throw new ExcecaoNegocio(NAO_ESTA_AGENDADO);
        }
        int horas = prazo.horasDeAntecedencia();
        if (!prazo.podeAlterar(agendamento, horas)) {
            throw new ExcecaoNegocio(FORA_DO_PRAZO.formatted(horas));
        }
        return agendamento;
    }

    /** A duração gravada na marcação vale também ao remarcar, mesmo que o serviço tenha mudado (RN02). */
    private static int duracaoDe(Agendamento agendamento) {
        return (int) Duration.between(agendamento.getInicio(), agendamento.getFim()).toMinutes();
    }
}
