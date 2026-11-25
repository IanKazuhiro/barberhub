package br.com.barberhub.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.barberhub.acesso.ClienteRepositorio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class HistoricoServicoTest {

    private static final long CARLA = 2L;
    private static final ZoneId MANAUS = ZoneId.of("America/Manaus");

    private final ClienteRepositorio clientes = mock(ClienteRepositorio.class);
    private final AgendamentoRepositorio agendamentos = mock(AgendamentoRepositorio.class);
    private final DisponibilidadeServico disponibilidade = mock(DisponibilidadeServico.class);
    private final HistoricoServico historico = new HistoricoServico(clientes, agendamentos, disponibilidade);

    @BeforeEach
    void preparar() {
        when(clientes.existsById(CARLA)).thenReturn(true);
        when(disponibilidade.local(any())).thenAnswer(
                i -> LocalDateTime.ofInstant(i.<Instant>getArgument(0), MANAUS));
    }

    @Test
    void mostraDataServicoBarbeiroEValorCobrado() {
        Agendamento a = new Agendamento();
        a.setId(9L);
        a.setInicio(Instant.parse("2026-11-10T14:00:00Z"));
        a.setPrecoCobrado(new BigDecimal("45.00"));
        when(agendamentos.doClientePorEstado(CARLA, EstadoAgendamento.CONCLUIDO))
                .thenReturn(List.of(new AgendamentoDetalhado(a, "Corte", "Rafael Souza")));

        AtendimentoResposta resposta = historico.doCliente(CARLA).get(0);

        assertEquals(LocalDateTime.of(2026, 11, 10, 10, 0), resposta.inicio());
        assertEquals("Corte", resposta.servico());
        assertEquals("Rafael Souza", resposta.barbeiro());
        assertEquals(new BigDecimal("45.00"), resposta.valor());
    }

    @Test
    void consultaSomenteOsConcluidos() {
        assertTrue(historico.doCliente(CARLA).isEmpty());

        verify(agendamentos).doClientePorEstado(CARLA, EstadoAgendamento.CONCLUIDO);
    }

    @Test
    void clienteInexistenteDaNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class, () -> historico.doCliente(99L));
    }
}
