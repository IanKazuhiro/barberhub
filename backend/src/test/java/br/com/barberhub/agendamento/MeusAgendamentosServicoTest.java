package br.com.barberhub.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeusAgendamentosServicoTest {

    private static final long CARLA = 2L;
    private static final ZoneId MANAUS = ZoneId.of("America/Manaus");
    private static final Instant AGORA = Instant.parse("2026-12-01T14:00:00Z");

    private final AgendamentoRepositorio agendamentos = mock(AgendamentoRepositorio.class);
    private final DisponibilidadeServico disponibilidade = mock(DisponibilidadeServico.class);
    private final PrazoServico prazo = mock(PrazoServico.class);
    private final MeusAgendamentosServico servico =
            new MeusAgendamentosServico(agendamentos, disponibilidade, prazo, Clock.fixed(AGORA, MANAUS));

    @BeforeEach
    void preparar() {
        when(disponibilidade.local(any())).thenAnswer(
                i -> LocalDateTime.ofInstant(i.<Instant>getArgument(0), MANAUS));
    }

    private static AgendamentoDetalhado agendamento(long id, Duration emQuantoTempo, EstadoAgendamento estado) {
        Agendamento a = new Agendamento();
        a.setId(id);
        a.setInicio(AGORA.plus(emQuantoTempo));
        a.setFim(a.getInicio().plus(Duration.ofMinutes(30)));
        a.setEstado(estado);
        a.setPrecoCobrado(new BigDecimal("45.00"));
        return new AgendamentoDetalhado(a, "Corte", "Rafael Souza");
    }

    private static List<Long> ids(List<AgendamentoResposta> lista) {
        return lista.stream().map(AgendamentoResposta::id).toList();
    }

    @Test
    void separaProximosEAnterioresEmOrdem() {
        when(agendamentos.doCliente(CARLA)).thenReturn(List.of(
                agendamento(1, Duration.ofDays(3), EstadoAgendamento.AGENDADO),
                agendamento(2, Duration.ofHours(5), EstadoAgendamento.AGENDADO),
                agendamento(3, Duration.ofDays(-10), EstadoAgendamento.CONCLUIDO),
                agendamento(4, Duration.ofDays(-2), EstadoAgendamento.FALTA)));

        MeusAgendamentosResposta resposta = servico.listar(CARLA);

        assertEquals(List.of(2L, 1L), ids(resposta.proximos()));
        assertEquals(List.of(4L, 3L), ids(resposta.anteriores()));
    }

    @Test
    void canceladoComDataFuturaVaiParaAnteriores() {
        when(agendamentos.doCliente(CARLA)).thenReturn(
                List.of(agendamento(1, Duration.ofDays(1), EstadoAgendamento.CANCELADO)));

        assertEquals(List.of(1L), ids(servico.listar(CARLA).anteriores()));
    }

    @Test
    void emAndamentoAindaEhProximoEOQueTerminouNao() {
        when(agendamentos.doCliente(CARLA)).thenReturn(List.of(
                agendamento(1, Duration.ofMinutes(-10), EstadoAgendamento.AGENDADO),
                agendamento(2, Duration.ofMinutes(-30), EstadoAgendamento.AGENDADO)));

        MeusAgendamentosResposta resposta = servico.listar(CARLA);

        assertEquals(List.of(1L), ids(resposta.proximos()));
        assertEquals(List.of(2L), ids(resposta.anteriores()));
    }

    @Test
    void mostraDataHorarioServicoBarbeiroEEstado() {
        when(agendamentos.doCliente(CARLA)).thenReturn(
                List.of(agendamento(1, Duration.ofHours(1), EstadoAgendamento.AGENDADO)));

        AgendamentoResposta a = servico.listar(CARLA).proximos().get(0);

        assertEquals(LocalDateTime.of(2026, 12, 1, 11, 0), a.inicio());
        assertEquals("Corte", a.servico());
        assertEquals("Rafael Souza", a.barbeiro());
        assertEquals(EstadoAgendamento.AGENDADO, a.estado());
    }

    @Test
    void dizOQueAindaPodeSerAlteradoEQualEhOPrazo() {
        AgendamentoDetalhado longe = agendamento(1, Duration.ofDays(1), EstadoAgendamento.AGENDADO);
        AgendamentoDetalhado perto = agendamento(2, Duration.ofHours(1), EstadoAgendamento.AGENDADO);
        when(agendamentos.doCliente(CARLA)).thenReturn(List.of(longe, perto));
        when(prazo.horasDeAntecedencia()).thenReturn(2);
        when(prazo.podeAlterar(longe.agendamento(), 2)).thenReturn(true);

        MeusAgendamentosResposta resposta = servico.listar(CARLA);

        assertEquals(2, resposta.antecedenciaMinimaHoras());
        assertEquals(List.of(false, true),
                resposta.proximos().stream().map(AgendamentoResposta::podeAlterar).toList());
    }

    @Test
    void semAgendamentosDevolveListasVazias() {
        when(agendamentos.doCliente(CARLA)).thenReturn(List.of());

        assertTrue(servico.listar(CARLA).proximos().isEmpty());
        assertTrue(servico.listar(CARLA).anteriores().isEmpty());
    }
}
