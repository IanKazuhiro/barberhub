package br.com.barberhub.catalogo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ComboResposta(Long id, String nome, BigDecimal preco, int duracaoMinutos,
        boolean ativo, Instant criadoEm, Instant atualizadoEm, List<ItemResposta> itens) {

    public record ItemResposta(Long id, String nome, int duracaoMinutos) {
    }

    public static ComboResposta de(Servico combo, List<Servico> itens) {
        List<ItemResposta> itensResposta = itens.stream()
                .map(s -> new ItemResposta(s.getId(), s.getNome(), s.getDuracaoMinutos()))
                .toList();
        return new ComboResposta(combo.getId(), combo.getNome(), combo.getPreco(), combo.getDuracaoMinutos(),
                combo.isAtivo(), combo.getCriadoEm(), combo.getAtualizadoEm(), itensResposta);
    }
}