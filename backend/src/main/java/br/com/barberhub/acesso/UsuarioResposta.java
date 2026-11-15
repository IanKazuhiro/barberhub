package br.com.barberhub.acesso;

public record UsuarioResposta(Long id, String nome, String email, String telefone, Perfil perfil) {
}