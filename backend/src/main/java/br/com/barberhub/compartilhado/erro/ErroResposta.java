package br.com.barberhub.compartilhado.erro;

import java.time.Instant;
import java.util.List;

public record ErroResposta(
        int status,
        String codigo,
        String mensagem,
        List<CampoInvalido> campos,
        Instant momento) {

    public record CampoInvalido(String campo, String mensagem) {
    }

    public static ErroResposta de(int status, String codigo, String mensagem) {
        return new ErroResposta(status, codigo, mensagem, List.of(), Instant.now());
    }
}