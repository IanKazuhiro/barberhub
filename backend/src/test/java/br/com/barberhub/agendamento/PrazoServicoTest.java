package br.com.barberhub.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class PrazoServicoTest {

    private static final Instant AGORA = Instant.parse("2026-12-01T14:00:00Z");

    private final ConfiguracaoRepositorio configuracoes = mock(ConfiguracaoRepositorio.class);
    private final PrazoServico prazo = new PrazoServico(configuracoes, Clock.fixed(AGORA, ZoneOffset.UTC));

    private static Agendamento daquiA(Duration tempo, EstadoAgendamento estado) {
        Agendamento a = new Agendamento();
        a.setInicio(AGORA.plus(tempo));
        a.setEstado(estado);
        return a;
    }

    @Test
    void leAAntecedenciaDaConfiguracao() {
        when(configuracoes.findById(PrazoServico.CHAVE))
                .thenReturn(Optional.of(new Configuracao(PrazoServico.CHAVE, " 3 ")));

        assertEquals(3, prazo.horasDeAntecedencia());
    }

    @Test
    void semConfiguracaoNaoInventaUmPrazo() {
        when(configuracoes.findById(PrazoServico.CHAVE)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, prazo::horasDeAntecedencia);
    }

    @Test
    void dentroDoPrazoPodeENoLimiteExatoTambem() {
        assertTrue(prazo.podeAlterar(daquiA(Duration.ofHours(5), EstadoAgendamento.AGENDADO), 2));
        assertTrue(prazo.podeAlterar(daquiA(Duration.ofHours(2), EstadoAgendamento.AGENDADO), 2));
    }

    @Test
    void foraDoPrazoNaoPode() {
        assertFalse(prazo.podeAlterar(daquiA(Duration.ofMinutes(119), EstadoAgendamento.AGENDADO), 2));
        assertFalse(prazo.podeAlterar(daquiA(Duration.ofHours(-1), EstadoAgendamento.AGENDADO), 2));
    }

    @Test
    void quemNaoEstaAgendadoNaoMuda() {
        for (EstadoAgendamento estado : EstadoAgendamento.values()) {
            assertEquals(estado == EstadoAgendamento.AGENDADO, prazo.podeAlterar(daquiA(Duration.ofDays(3), estado), 2));
        }
    }
}
