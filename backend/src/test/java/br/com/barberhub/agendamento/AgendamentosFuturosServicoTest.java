package br.com.barberhub.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.barberhub.barbeiro.BarbeiroRepositorio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class AgendamentosFuturosServicoTest {

    private static final long RAFAEL = 5L;
    private static final ZoneId MANAUS = ZoneId.of("America/Manaus");
    private static final Instant AGORA = Instant.parse("2026-12-01T14:00:00Z");

    private final BarbeiroRepositorio barbeiros = mock(BarbeiroRepositorio.class);
    private final AgendamentoRepositorio agendamentos = mock(AgendamentoRepositorio.class);
    private final DisponibilidadeServico disponibilidade = mock(DisponibilidadeServico.class);
    private final AgendamentosFuturosServico servico = new AgendamentosFuturosServico(barbeiros, agendamentos,
            disponibilidade, Clock.fixed(AGORA, MANAUS));

    @Test
    void listaOsEmAbertoAPartirDeAgoraComOContatoDoCliente() {
        Agendamento a = new Agendamento();
        a.setId(9L);
        a.setInicio(Instant.parse("2026-12-02T14:00:00Z"));
        a.setFim(Instant.parse("2026-12-02T14:30:00Z"));
        when(barbeiros.existsById(RAFAEL)).thenReturn(true);
        when(disponibilidade.local(any())).thenAnswer(
                i -> LocalDateTime.ofInstant(i.<Instant>getArgument(0), MANAUS));
        when(agendamentos.emAbertoDoBarbeiro(RAFAEL, AGORA)).thenReturn(List.of(
                new AgendamentoComCliente(a, "Carla Mendes", "92988887777", null, null, "Corte")));

        AgendamentoFuturoResposta resposta = servico.doBarbeiro(RAFAEL).get(0);

        assertEquals(LocalDateTime.of(2026, 12, 2, 10, 0), resposta.inicio());
        assertEquals("Carla Mendes", resposta.cliente());
        assertEquals("92988887777", resposta.telefone());
        assertEquals("Corte", resposta.servico());
    }

    @Test
    void barbeiroInexistenteDaNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.doBarbeiro(99L));
    }
}
