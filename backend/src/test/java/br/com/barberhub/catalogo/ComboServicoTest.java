package br.com.barberhub.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class ComboServicoTest {

    private final ServicoRepositorio servicos = mock(ServicoRepositorio.class);
    private final ComboItemRepositorio itens = mock(ComboItemRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final ComboServico combos = new ComboServico(servicos, itens, auditoria);

    private static Servico servicoAtivo(Long id, String nome, int duracao) {
        Servico s = new Servico();
        s.setId(id);
        s.setNome(nome);
        s.setDuracaoMinutos((short) duracao);
        s.setPreco(new BigDecimal("20.00"));
        s.setAtivo(true);
        return s;
    }

    private static ComboRequisicao combo(String nome, List<Long> ids) {
        return new ComboRequisicao(nome, new BigDecimal("70.00"), ids);
    }

    @Test
    void criaComboComDuracaoSomadaEAudita() {
        Servico corte = servicoAtivo(1L, "Corte", 30);
        Servico barba = servicoAtivo(2L, "Barba", 20);
        when(servicos.findAllById(List.of(1L, 2L))).thenReturn(List.of(corte, barba));
        when(servicos.saveAndFlush(any())).thenAnswer(i -> {
            Servico s = i.getArgument(0);
            s.setId(10L);
            return s;
        });

        ComboResposta resposta = combos.criar(1L, combo("Corte + Barba", List.of(1L, 2L)));

        assertEquals(50, resposta.duracaoMinutos());
        assertEquals(2, resposta.itens().size());
        verify(auditoria).registrar(1L, "CRIAR_COMBO", "servico", 10L);
    }

    @Test
    void recusaMenosDeDoisItensValidos() {
        when(servicos.findAllById(List.of(1L))).thenReturn(List.of(servicoAtivo(1L, "Corte", 30)));

        assertThrows(ExcecaoNegocio.class, () -> combos.criar(1L, combo("Só um", List.of(1L))));
        verify(servicos, never()).saveAndFlush(any());
    }

    @Test
    void recusaItemRepetido() {
        assertThrows(ExcecaoNegocio.class, () -> combos.criar(1L, combo("Repetido", List.of(1L, 1L))));
    }

    @Test
    void recusaItemInativo() {
        Servico inativo = servicoAtivo(2L, "Barba", 20);
        inativo.setAtivo(false);
        when(servicos.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(servicoAtivo(1L, "Corte", 30), inativo));

        assertThrows(ExcecaoNegocio.class, () -> combos.criar(1L, combo("Com inativo", List.of(1L, 2L))));
    }

    @Test
    void recusaComboDentroDeCombo() {
        Servico outroCombo = servicoAtivo(2L, "Outro combo", 50);
        outroCombo.setEhCombo(true);
        when(servicos.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(servicoAtivo(1L, "Corte", 30), outroCombo));

        assertThrows(ExcecaoNegocio.class, () -> combos.criar(1L, combo("Aninhado", List.of(1L, 2L))));
    }

    @Test
    void alterarRecriaOsItensERecalculaADuracao() {
        Servico comboExistente = servicoAtivo(10L, "Combo antigo", 50);
        comboExistente.setEhCombo(true);
        when(servicos.findById(10L)).thenReturn(Optional.of(comboExistente));
        when(servicos.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        Servico novoItem = servicoAtivo(3L, "Sobrancelha", 15);
        when(servicos.findAllById(List.of(3L, 1L))).thenReturn(List.of(novoItem, servicoAtivo(1L, "Corte", 30)));

        ComboResposta resposta = combos.alterar(1L, 10L, combo("Combo novo", List.of(3L, 1L)));

        assertEquals(45, resposta.duracaoMinutos());
        verify(itens).deleteByComboId(10L);
        verify(auditoria).registrar(1L, "ALTERAR_COMBO", "servico", 10L);
    }

    @Test
    void alterarServicoQueNaoEhComboDaNaoEncontrado() {
        when(servicos.findById(5L)).thenReturn(Optional.of(servicoAtivo(5L, "Corte", 30)));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> combos.alterar(1L, 5L, combo("X", List.of(1L, 2L))));
    }

    @Test
    void listaSoCombos() {
        Servico combo = servicoAtivo(10L, "Combo", 50);
        combo.setEhCombo(true);
        when(servicos.findByEhComboTrueOrderByNomeAsc()).thenReturn(List.of(combo));
        when(itens.findById_ComboId(10L)).thenReturn(List.of(new ComboItem(10L, 1L)));
        when(servicos.findAllById(List.of(1L))).thenReturn(List.of(servicoAtivo(1L, "Corte", 30)));

        List<ComboResposta> resultado = combos.listar(null);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).itens().stream().anyMatch(i -> i.id().equals(1L)));
    }
}