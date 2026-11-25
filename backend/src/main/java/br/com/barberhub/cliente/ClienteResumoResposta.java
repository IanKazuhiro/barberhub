package br.com.barberhub.cliente;

import br.com.barberhub.acesso.Usuario;

public record ClienteResumoResposta(Long id, String nome, String email, String telefone, boolean ativo) {

    static ClienteResumoResposta de(Usuario u) {
        return new ClienteResumoResposta(u.getId(), u.getNome(), u.getEmail(), u.getTelefone(), u.isAtivo());
    }
}
