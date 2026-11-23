package br.com.barberhub.catalogo;

import java.math.BigDecimal;
import java.time.Instant;

public record ServicoResposta(Long id, String nome, int duracaoMinutos, BigDecimal preco,
        boolean ehCombo, boolean ativo, Instant criadoEm, Instant atualizadoEm) {

    public static ServicoResposta de(Servico s) {
        return new ServicoResposta(s.getId(), s.getNome(), s.getDuracaoMinutos(), s.getPreco(),
                s.isEhCombo(), s.isAtivo(), s.getCriadoEm(), s.getAtualizadoEm());
    }
}