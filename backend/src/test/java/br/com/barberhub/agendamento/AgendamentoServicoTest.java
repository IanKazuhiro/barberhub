package br.com.barberhub.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.catalogo.Servico;
import br.com.barberhub.compartilhado.erro.ConflitoException;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;

class AgendamentoServicoTest {

    private static final long CARLA = 2L;
    private static final long RAFAEL = 5L;
    private static final long CORTE = 1L;
    private static final ZoneId MANAUS = ZoneId.of("America/Manaus");
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 2, 10, 0);

    private final AgendamentoRepositorio agendamentos = mock(AgendamentoRepositorio.class);
    private final DisponibilidadeServico disponibilidade = mock(DisponibilidadeServico.class);
    private final PrazoServico prazo = mock(PrazoServico.class);
    private final AgendamentoServico servico = new AgendamentoServico(agendamentos, disponibilidade, prazo);

    private final NovoAgendamentoRequisicao pedido = new NovoAgendamentoRequisicao(CORTE, RAFAEL, INICIO);

    @BeforeEach
    void preparar() {
        Servico corte = new Servico();
        corte.setId(CORTE);
        corte.setNome("Corte");
        corte.setDuracaoMinutos((short) 45);
        corte.setPreco(new BigDecimal("50.00"));
        when(disponibilidade.servicoAtivo(CORTE)).thenReturn(corte);

        Usuario rafael = new Usuario();
        rafael.setId(RAFAEL);
        rafael.setNome("Rafael Souza");
        when(disponibilidade.exigirBarbeiroAtivo(RAFAEL)).thenReturn(rafael);

        when(disponibilidade.instante(any())).thenAnswer(
                i -> i.<LocalDateTime>getArgument(0).atZone(MANAUS).toInstant());
        when(disponibilidade.local(any())).thenAnswer(
                i -> LocalDateTime.ofInstant(i.<Instant>getArgument(0), MANAUS));
        when(agendamentos.saveAndFlush(any())).thenAnswer(i -> {
            Agendamento a = i.getArgument(0);
            a.setId(9L);
            return a;
        });
    }

    @Test
    void criaAgendadoComFimPelaDuracaoEPrecoDoMomento() {
        AgendamentoResposta resposta = servico.agendar(CARLA, pedido);

        ArgumentCaptor<Agendamento> gravado = ArgumentCaptor.forClass(Agendamento.class);
        verify(agendamentos).saveAndFlush(gravado.capture());
        Agendamento a = gravado.getValue();
        assertEquals(EstadoAgendamento.AGENDADO, a.getEstado());
        assertEquals(CARLA, a.getClienteId());
        assertEquals(CARLA, a.getCriadoPor());
        assertEquals(OrigemAgendamento.APP, a.getOrigem());
        assertEquals(new BigDecimal("50.00"), a.getPrecoCobrado());
        // 10h em Manaus são 14h em UTC.
        assertEquals(Instant.parse("2026-12-02T14:00:00Z"), a.getInicio());
        assertEquals(Instant.parse("2026-12-02T14:45:00Z"), a.getFim());

        assertEquals(INICIO, resposta.inicio());
        assertEquals(INICIO.plusMinutes(45), resposta.fim());
        assertEquals("Rafael Souza", resposta.barbeiro());
    }

    @Test
    void confereADisponibilidadeAntesDeGravar() {
        doThrow(new ExcecaoNegocio(DisponibilidadeServico.HORARIO_INVALIDO))
                .when(disponibilidade).exigirLivre(RAFAEL, 45, INICIO, null);

        assertThrows(ExcecaoNegocio.class, () -> servico.agendar(CARLA, pedido));
        verify(agendamentos, never()).saveAndFlush(any());
    }

    @Test
    void quandoOBancoRecusaOSegundoPedidoSimultaneoAvisaQueFoiOcupado() {
        doThrow(new DataIntegrityViolationException("ex_agendamento")).when(agendamentos).saveAndFlush(any());

        ConflitoException erro = assertThrows(ConflitoException.class, () -> servico.agendar(CARLA, pedido));
        assertEquals(DisponibilidadeServico.HORARIO_OCUPADO, erro.getMessage());
    }

    @Test
    void deadlockEntreDuasInsercoesDaOMesmoAviso() {
        doThrow(new CannotAcquireLockException("deadlock")).when(agendamentos).saveAndFlush(any());

        assertThrows(ConflitoException.class, () -> servico.agendar(CARLA, pedido));
    }
}
