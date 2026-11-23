package br.com.barberhub.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class ServicoServicoTest {

    private final ServicoRepositorio servicos = mock(ServicoRepositorio.class);
    private final ComboItemRepositorio itensDeCombo = mock(ComboItemRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final ServicoServico servico = new ServicoServico(servicos, itensDeCombo, auditoria);

    private static ServicoRequisicao corte() {
        return new ServicoRequisicao("  Corte masculino ", 30, new BigDecimal("40.00"));
    }

    private static Servico existente(Long id, boolean ativo) {
        Servico s = new Servico();
        s.setId(id);
        s.setNome("Barba");
        s.setDuracaoMinutos((short) 20);
        s.setPreco(new BigDecimal("25.00"));
        s.setAtivo(ativo);
        return s;
    }

    @Test
    void criaServicoAtivoSemComboEAudita() {
        when(servicos.saveAndFlush(any())).thenAnswer(i -> {
            Servico s = i.getArgument(0);
            s.setId(7L);
            return s;
        });

        ServicoResposta resposta = servico.criar(1L, corte());

        assertEquals(7L, resposta.id());
        assertEquals("Corte masculino", resposta.nome());
        assertEquals(30, resposta.duracaoMinutos());
        assertTrue(resposta.ativo());
        assertFalse(resposta.ehCombo());
        verify(servicos).existsByNomeIgnoreCase("Corte masculino");
        verify(auditoria).registrar(1L, "CRIAR_SERVICO", "servico", 7L);
    }

    @Test
    void recusaNomeRepetidoAoCriar() {
        when(servicos.existsByNomeIgnoreCase("Corte masculino")).thenReturn(true);

        assertThrows(ExcecaoNegocio.class, () -> servico.criar(1L, corte()));
        verify(servicos, never()).saveAndFlush(any());
    }

    @Test
    void recusaNomeRepetidoQuandoOBancoBarra() {
        when(servicos.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq_servico_nome"));

        assertThrows(ExcecaoNegocio.class, () -> servico.criar(1L, corte()));
        verify(auditoria, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void alteraOsDadosEAudita() {
        when(servicos.findById(3L)).thenReturn(Optional.of(existente(3L, true)));
        when(servicos.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        ServicoResposta resposta = servico.alterar(1L, 3L, corte());

        assertEquals("Corte masculino", resposta.nome());
        assertEquals(30, resposta.duracaoMinutos());
        assertEquals(0, new BigDecimal("40.00").compareTo(resposta.preco()));
        verify(servicos).existsByNomeIgnoreCaseAndIdNot("Corte masculino", 3L);
        verify(auditoria).registrar(1L, "ALTERAR_SERVICO", "servico", 3L);
    }

    @Test
    void recusaAlterarParaNomeDeOutroServico() {
        when(servicos.findById(3L)).thenReturn(Optional.of(existente(3L, true)));
        when(servicos.existsByNomeIgnoreCaseAndIdNot("Corte masculino", 3L)).thenReturn(true);

        assertThrows(ExcecaoNegocio.class, () -> servico.alterar(1L, 3L, corte()));
        verify(servicos, never()).saveAndFlush(any());
    }

    @Test
    void alterarServicoInexistenteDaNaoEncontrado() {
        when(servicos.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.alterar(1L, 99L, corte()));
    }

    @Test
    void inativaEAudita() {
        Servico atual = existente(3L, true);
        when(servicos.findById(3L)).thenReturn(Optional.of(atual));

        ServicoResposta resposta = servico.alterarSituacao(1L, 3L, false);

        assertFalse(resposta.ativo());
        verify(servicos).save(atual);
        verify(auditoria).registrar(1L, "INATIVAR_SERVICO", "servico", 3L);
    }

    @Test
    void reativaEAudita() {
        Servico atual = existente(3L, false);
        when(servicos.findById(3L)).thenReturn(Optional.of(atual));

        ServicoResposta resposta = servico.alterarSituacao(1L, 3L, true);

        assertTrue(resposta.ativo());
        verify(auditoria).registrar(1L, "ATIVAR_SERVICO", "servico", 3L);
    }

    @Test
    void naoAuditaQuandoASituacaoJaEraEssa() {
        when(servicos.findById(3L)).thenReturn(Optional.of(existente(3L, false)));

        servico.alterarSituacao(1L, 3L, false);

        verify(servicos, never()).save(any());
        verify(auditoria, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void listaTodosOuFiltraPorSituacao() {
        when(servicos.findAllByOrderByNomeAsc())
                .thenReturn(List.of(existente(1L, true), existente(2L, false)));
        when(servicos.findByAtivoOrderByNomeAsc(true)).thenReturn(List.of(existente(1L, true)));

        assertEquals(2, servico.listar(null).size());
        assertEquals(1, servico.listar(true).size());
    }

    @Test
    void recusaDesativarServicoDentroDeComboAtivo() {
        when(servicos.findById(3L)).thenReturn(Optional.of(existente(3L, true)));
        when(itensDeCombo.existeComboAtivoComServico(3L)).thenReturn(true);

        assertThrows(ExcecaoNegocio.class, () -> servico.alterarSituacao(1L, 3L, false));
        verify(servicos, never()).save(any());
    }

    @Test
    void recusaAlterarUmCombo() {
        Servico combo = existente(10L, true);
        combo.setEhCombo(true);
        when(servicos.findById(10L)).thenReturn(Optional.of(combo));

        assertThrows(ExcecaoNegocio.class, () -> servico.alterar(1L, 10L, corte()));
    }
}