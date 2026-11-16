package br.com.barberhub.acesso;

public record LoginResposta(String token, String tipo, long expiraEmSegundos, UsuarioResposta usuario) {
}