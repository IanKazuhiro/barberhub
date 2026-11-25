package br.com.barberhub.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ConflitoException;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class AlteracaoAgendamentoServicoTest {

    private static final long CARLA = 2L;
    private static final long RAFAEL = 5L;
    private static final long ID = 9L;
    private static final ZoneId MANAUS = ZoneId.of("America/Manaus");
    private static final Instant AGORA = Instant.parse("2026-12-01T14:00:00Z");
    private static final LocalDateTime ATUAL = LocalDateTime.of(2026, 12, 2, 10, 0);
    private static final LocalDateTime NOVO = LocalDateTime.of(2026, 12, 3, 15, 0);

    private final AgendamentoRepositorio agendamentos = mock(AgendamentoRepositorio.class);
    private final DisponibilidadeServico disponibilidade = mock(DisponibilidadeServico.class);
    private final PrazoServico prazo = mock(PrazoServico.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final AlteracaoAgendamentoServico servico = new AlteracaoAgendamentoServico(agendamentos,
            disponibilidade, prazo, auditoria, Clock.fixed(AGORA, MANAUS));

    private final Agendamento agendamento = new Agendamento();

    @BeforeEach
    void preparar() {
        agendamento.setId(ID);
        agendamento.setClienteId(CARLA);
        agendamento.setBarbeiroId(RAFAEL);
        agendamento.setEstado(EstadoAgendamento.AGENDADO);
        agendamento.setInicio(ATUAL.atZone(MANAUS).toInstant());
        agendamento.setFim(ATUAL.plusMinutes(45).atZone(MANAUS).toInstant());
        when(agendamentos.travarDoCliente(ID, CARLA)).thenReturn(Optional.of(agendamento));
        when(agendamentos.findByIdAndClienteId(ID, CARLA)).thenReturn(Optional.of(agendamento));
        when(prazo.horasDeAntecedencia()).thenReturn(2);
        when(prazo.podeAlterar(agendamento, 2)).thenReturn(true);
        when(disponibilidade.instante(any())).thenAnswer(
                i -> i.<LocalDateTime>getArgument(0).atZone(MANAUS).toInstant());
        when(disponibilidade.local(any())).thenAnswer(
                i -> LocalDateTime.ofInstant(i.<Instant>getArgument(0), MANAUS));
    }

    @Test
    void cancelaGuardaOMomentoERegistraAuditoria() {
        servico.cancelar(CARLA, ID);

        assertEquals(EstadoAgendamento.CANCELADO, agendamento.getEstado());
        assertEquals(AGORA, agendamento.getCanceladoEm());
        verify(auditoria).registrar(CARLA, "CANCELAR_AGENDAMENTO", "agendamento", ID);
    }

    @Test
    void foraDoPrazoNaoCancelaNemRemarcaEDizOPrazo() {
        when(prazo.podeAlterar(agendamento, 2)).thenReturn(false);

        ExcecaoNegocio erro = assertThrows(ExcecaoNegocio.class, () -> servico.cancelar(CARLA, ID));
        assertTrue(erro.getMessage().contains("2 hora(s)"));
        assertThrows(ExcecaoNegocio.class, () -> servico.remarcar(CARLA, ID, new RemarcarRequisicao(NOVO)));
        assertEquals(EstadoAgendamento.AGENDADO, agendamento.getEstado());
        verify(auditoria, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void quemJaSaiuDeAgendadoNaoVolta() {
        for (EstadoAgendamento estado : List.of(EstadoAgendamento.CANCELADO, EstadoAgendamento.CONCLUIDO,
                EstadoAgendamento.FALTA)) {
            agendamento.setEstado(estado);

            assertThrows(ExcecaoNegocio.class, () -> servico.cancelar(CARLA, ID));
            assertThrows(ExcecaoNegocio.class, () -> servico.remarcar(CARLA, ID, new RemarcarRequisicao(NOVO)));
            assertEquals(estado, agendamento.getEstado());
        }
    }

    @Test
    void agendamentoDeOutroClienteDaNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.cancelar(77L, ID));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.remarcar(77L, ID, new RemarcarRequisicao(NOVO)));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.horariosParaRemarcar(77L, ID, LocalDate.of(2026, 12, 3)));
    }

    @Test
    void remarcaNaMesmaLinhaMantendoADuracao() {
        servico.remarcar(CARLA, ID, new RemarcarRequisicao(NOVO));

        verify(disponibilidade).exigirBarbeiroAtivo(RAFAEL);
        verify(disponibilidade).exigirLivre(RAFAEL, 45, NOVO, ID);
        assertEquals(NOVO.atZone(MANAUS).toInstant(), agendamento.getInicio());
        assertEquals(NOVO.plusMinutes(45).atZone(MANAUS).toInstant(), agendamento.getFim());
        assertEquals(EstadoAgendamento.AGENDADO, agendamento.getEstado());
        verify(agendamentos).saveAndFlush(agendamento);
        verify(auditoria).registrar(CARLA, "REMARCAR_AGENDAMENTO", "agendamento", ID);
    }

    @Test
    void recusaRemarcarParaOMesmoHorario() {
        ExcecaoNegocio erro = assertThrows(ExcecaoNegocio.class,
                () -> servico.remarcar(CARLA, ID, new RemarcarRequisicao(ATUAL)));

        assertEquals(AlteracaoAgendamentoServico.MESMO_HORARIO, erro.getMessage());
        verify(agendamentos, never()).saveAndFlush(any());
    }

    @Test
    void horarioNovoOcupadoNaoMudaNada() {
        doThrow(new ConflitoException(DisponibilidadeServico.HORARIO_OCUPADO))
                .when(disponibilidade).exigirLivre(RAFAEL, 45, NOVO, ID);

        assertThrows(ConflitoException.class, () -> servico.remarcar(CARLA, ID, new RemarcarRequisicao(NOVO)));
        assertEquals(ATUAL.atZone(MANAUS).toInstant(), agendamento.getInicio());
        verify(agendamentos, never()).saveAndFlush(any());
    }

    @Test
    void perderOHorarioNaGravacaoDaConflito() {
        doThrow(new DataIntegrityViolationException("ex_agendamento")).when(agendamentos).saveAndFlush(any());

        assertThrows(ConflitoException.class, () -> servico.remarcar(CARLA, ID, new RemarcarRequisicao(NOVO)));
        verify(auditoria, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void naoRemarcaComBarbeiroInativo() {
        doThrow(new ExcecaoNegocio(DisponibilidadeServico.BARBEIRO_INDISPONIVEL))
                .when(disponibilidade).exigirBarbeiroAtivo(RAFAEL);

        assertThrows(ExcecaoNegocio.class, () -> servico.remarcar(CARLA, ID, new RemarcarRequisicao(NOVO)));
        verify(disponibilidade, never()).exigirLivre(any(), anyInt(), any(), any());
    }

    @Test
    void horariosParaRemarcarNaoTrazemOAtual() {
        LocalDate dia = ATUAL.toLocalDate();
        when(disponibilidade.horariosLivres(RAFAEL, 45, dia, ID))
                .thenReturn(List.of(LocalTime.of(9, 45), LocalTime.of(10, 0), LocalTime.of(10, 15)));

        assertEquals(List.of(LocalTime.of(9, 45), LocalTime.of(10, 15)),
                servico.horariosParaRemarcar(CARLA, ID, dia));
    }
}
