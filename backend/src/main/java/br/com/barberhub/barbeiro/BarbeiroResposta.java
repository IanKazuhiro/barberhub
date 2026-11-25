package br.com.barberhub.barbeiro;

import java.math.BigDecimal;

import br.com.barberhub.acesso.Usuario;

public record BarbeiroResposta(Long id, String nome, String email, String telefone,
        BigDecimal percentualComissao, boolean ativo, boolean possuiEscala) {

    public static BarbeiroResposta de(Usuario u, Barbeiro b, boolean possuiEscala) {
        return new BarbeiroResposta(u.getId(), u.getNome(), u.getEmail(), u.getTelefone(),
                b.getPercentualComissao(), u.isAtivo(), possuiEscala);
    }
}
