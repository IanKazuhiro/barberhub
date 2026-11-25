package br.com.barberhub.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgendaServicoTest {

    private static final long RAFAEL = 5L;
    private static final ZoneId MANAUS = ZoneId.of("America/Manaus");
    /** 22h de 01/12 em Manaus, quando em UTC já é 02/12. */
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-12-02T02:00:00Z"), MANAUS);

    private final AgendamentoRepositorio agendamentos = mock(AgendamentoRepositorio.class);
    private final DisponibilidadeServico disponibilidade = mock(DisponibilidadeServico.class);
    private final AgendaServico agenda = new AgendaServico(agendamentos, disponibilidade, RELOGIO);

    @BeforeEach
    void preparar() {
        when(disponibilidade.instante(any())).thenAnswer(
                i -> i.<LocalDateTime>getArgument(0).atZone(MANAUS).toInstant());
        when(disponibilidade.local(any())).thenAnswer(
                i -> LocalDateTime.ofInstant(i.<Instant>getArgument(0), MANAUS));
    }

    @Test
    void semDataUsaOHojeDaBarbearia() {
        agenda.doDia(RAFAEL, null);

        // De 01/12 0h a 02/12 0h em Manaus.
        verify(agendamentos).agendaDoBarbeiro(RAFAEL,
                Instant.parse("2026-12-01T04:00:00Z"), Instant.parse("2026-12-02T04:00:00Z"));
    }

    @Test
    void comDataUsaODiaPedidoDoProprioBarbeiro() {
        agenda.doDia(RAFAEL, LocalDate.of(2026, 12, 10));

        verify(agendamentos).agendaDoBarbeiro(RAFAEL,
                Instant.parse("2026-12-10T04:00:00Z"), Instant.parse("2026-12-11T04:00:00Z"));
    }

    @Test
    void mostraHorarioClienteServicoEstadoEObservacoes() {
        Agendamento a = new Agendamento();
        a.setId(9L);
        a.setClienteId(2L);
        a.setInicio(Instant.parse("2026-12-01T14:00:00Z"));
        a.setFim(Instant.parse("2026-12-01T14:30:00Z"));
        a.setEstado(EstadoAgendamento.AGENDADO);
        when(agendamentos.agendaDoBarbeiro(any(), any(), any())).thenReturn(List.of(
                new AgendamentoComCliente(a, "Carla Mendes", "92988887777", "Degradê", "Prefere tesoura", "Corte")));

        ItemAgendaResposta item = agenda.doDia(RAFAEL, null).get(0);

        assertEquals(LocalTime.of(10, 0), item.inicio());
        assertEquals(LocalTime.of(10, 30), item.fim());
        assertEquals("Carla Mendes", item.cliente());
        assertEquals("Corte", item.servico());
        assertEquals(EstadoAgendamento.AGENDADO, item.estado());
        assertEquals("Prefere tesoura", item.observacoes());
    }
}
