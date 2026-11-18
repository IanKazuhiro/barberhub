package br.com.barberhub.acesso;

import java.time.Instant;

public record UsuarioAdminResposta(Long id, String nome, String email, String telefone,
        Perfil perfil, boolean ativo, Instant criadoEm) {

    public static UsuarioAdminResposta de(Usuario u) {
        return new UsuarioAdminResposta(u.getId(), u.getNome(), u.getEmail(), u.getTelefone(),
                u.getPerfil(), u.isAtivo(), u.getCriadoEm());
    }
}