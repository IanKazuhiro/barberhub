package br.com.barberhub.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class CatalogoServicoTest {

    private final ServicoRepositorio servicos = mock(ServicoRepositorio.class);
    private final ComboItemRepositorio itens = mock(ComboItemRepositorio.class);
    private final CatalogoServico catalogo = new CatalogoServico(servicos, itens);

    private static Servico servico(Long id, String nome, int duracao, boolean ehCombo) {
        Servico s = new Servico();
        s.setId(id);
        s.setNome(nome);
        s.setDuracaoMinutos((short) duracao);
        s.setPreco(new BigDecimal("45.00"));
        s.setEhCombo(ehCombo);
        return s;
    }

    @Test
    void mostraNomeDuracaoEPrecoDosServicosAtivos() {
        when(servicos.findByAtivoOrderByNomeAsc(true)).thenReturn(List.of(servico(1L, "Corte", 30, false)));

        List<ItemCatalogoResposta> lista = catalogo.listar();

        assertEquals(1, lista.size());
        assertEquals("Corte", lista.get(0).nome());
        assertEquals(30, lista.get(0).duracaoMinutos());
        assertEquals(new BigDecimal("45.00"), lista.get(0).preco());
        assertTrue(lista.get(0).itens().isEmpty());
    }

    @Test
    void consultaSomenteOsAtivos() {
        when(servicos.findByAtivoOrderByNomeAsc(true)).thenReturn(List.of());

        assertTrue(catalogo.listar().isEmpty());

        verify(servicos).findByAtivoOrderByNomeAsc(true);
        verify(servicos, never()).findAllByOrderByNomeAsc();
    }

    @Test
    void comboTrazOsNomesDosSeusServicos() {
        Servico corte = servico(1L, "Corte", 30, false);
        Servico barba = servico(2L, "Barba", 15, false);
        Servico combo = servico(8L, "Cabelo e barba", 45, true);
        when(servicos.findByAtivoOrderByNomeAsc(true)).thenReturn(List.of(barba, combo, corte));
        when(itens.findById_ComboIdIn(List.of(8L)))
                .thenReturn(List.of(new ComboItem(8L, 1L), new ComboItem(8L, 2L)));
        when(servicos.findAllById(List.of(1L, 2L))).thenReturn(List.of(corte, barba));

        ItemCatalogoResposta resposta = catalogo.listar().stream()
                .filter(ItemCatalogoResposta::ehCombo).findFirst().orElseThrow();

        assertEquals(List.of("Corte", "Barba"), resposta.itens());
    }

    @Test
    void semCombosNaoConsultaItens() {
        when(servicos.findByAtivoOrderByNomeAsc(true)).thenReturn(List.of(servico(1L, "Corte", 30, false)));

        catalogo.listar();

        verify(itens, never()).findById_ComboIdIn(any());
    }
}
