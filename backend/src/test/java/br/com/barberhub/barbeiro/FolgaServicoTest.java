package br.com.barberhub.barbeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class FolgaServicoTest {

    private static final long ADMIN = 1L;
    private static final long RAFAEL = 5L;
    // 02:00 em UTC de 02/12 ainda é 22:00 de 01/12 em Manaus.
    private static final Clock RELOGIO =
            Clock.fixed(Instant.parse("2026-12-02T02:00:00Z"), ZoneId.of("America/Manaus"));
    private static final LocalDate HOJE = LocalDate.of(2026, 12, 1);

    private final BarbeiroRepositorio barbeiros = mock(BarbeiroRepositorio.class);
    private final FolgaBarbeiroRepositorio folgas = mock(FolgaBarbeiroRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final FolgaServico servico = new FolgaServico(barbeiros, folgas, auditoria, RELOGIO);

    @BeforeEach
    void preparar() {
        when(barbeiros.existsById(RAFAEL)).thenReturn(true);
        when(folgas.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void criaFolgaERegistraAuditoria() {
        FolgaResposta resposta = servico.criar(ADMIN, RAFAEL, new FolgaRequisicao(HOJE.plusDays(3), " Consulta "));

        assertEquals(HOJE.plusDays(3), resposta.data());
        assertEquals("Consulta", resposta.motivo());
        verify(auditoria).registrar(ADMIN, "CRIAR_FOLGA", "barbeiro", RAFAEL);
    }

    @Test
    void motivoEmBrancoViraNulo() {
        assertNull(servico.criar(ADMIN, RAFAEL, new FolgaRequisicao(HOJE.plusDays(3), "  ")).motivo());
    }

    @Test
    void hojeNoFusoDaBarbeariaEPermitido() {
        assertEquals(HOJE, servico.criar(ADMIN, RAFAEL, new FolgaRequisicao(HOJE, null)).data());
    }

    @Test
    void recusaFolgaNoPassado() {
        ExcecaoNegocio erro = assertThrows(ExcecaoNegocio.class,
                () -> servico.criar(ADMIN, RAFAEL, new FolgaRequisicao(HOJE.minusDays(1), null)));

        assertEquals(FolgaServico.NO_PASSADO, erro.getMessage());
        verify(folgas, never()).saveAndFlush(any());
    }

    @Test
    void recusaFolgaRepetida() {
        when(folgas.existsByBarbeiroIdAndData(RAFAEL, HOJE)).thenReturn(true);

        assertThrows(ExcecaoNegocio.class, () -> servico.criar(ADMIN, RAFAEL, new FolgaRequisicao(HOJE, null)));
        verify(folgas, never()).saveAndFlush(any());
    }

    @Test
    void duasFolgasIguaisAoMesmoTempoOBancoRecusaASegunda() {
        when(folgas.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq"));

        ExcecaoNegocio erro = assertThrows(ExcecaoNegocio.class,
                () -> servico.criar(ADMIN, RAFAEL, new FolgaRequisicao(HOJE, null)));

        assertEquals(FolgaServico.REPETIDA, erro.getMessage());
        verify(auditoria, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void listaSoDeHojeEmDiante() {
        when(folgas.findByBarbeiroIdAndDataGreaterThanEqualOrderByDataAsc(RAFAEL, HOJE))
                .thenReturn(List.of(new FolgaBarbeiro(RAFAEL, HOJE, null)));

        assertEquals(1, servico.listar(RAFAEL).size());
    }

    @Test
    void removeAFolgaDoProprioBarbeiroERegistraAuditoria() {
        FolgaBarbeiro folga = new FolgaBarbeiro(RAFAEL, HOJE, null);
        when(folgas.findByIdAndBarbeiroId(7L, RAFAEL)).thenReturn(Optional.of(folga));

        servico.remover(ADMIN, RAFAEL, 7L);

        verify(folgas).delete(folga);
        verify(auditoria).registrar(ADMIN, "REMOVER_FOLGA", "barbeiro", RAFAEL);
    }

    @Test
    void removerFolgaDeOutroBarbeiroDaNaoEncontrado() {
        when(folgas.findByIdAndBarbeiroId(7L, RAFAEL)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.remover(ADMIN, RAFAEL, 7L));
    }

    @Test
    void barbeiroInexistenteDaNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.criar(ADMIN, 99L, new FolgaRequisicao(HOJE, null)));
    }
}
