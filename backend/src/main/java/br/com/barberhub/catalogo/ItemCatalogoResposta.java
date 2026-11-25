package br.com.barberhub.catalogo;

import java.math.BigDecimal;
import java.util.List;

/** O que o cliente vê no catálogo. `itens` traz os nomes dos serviços de um combo (vazio no serviço simples). */
public record ItemCatalogoResposta(Long id, String nome, int duracaoMinutos, BigDecimal preco,
        boolean ehCombo, List<String> itens) {

    public static ItemCatalogoResposta de(Servico s, List<String> itens) {
        return new ItemCatalogoResposta(s.getId(), s.getNome(), s.getDuracaoMinutos(), s.getPreco(),
                s.isEhCombo(), itens);
    }
}
