package br.com.barberhub.cliente;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** O cliente só alcança o próprio cadastro: o id vem do token, nunca da requisição. */
@RestController
@RequestMapping("/api/cliente/dados")
@RequiredArgsConstructor
public class MeusDadosController {

    private final MeusDadosServico meusDados;

    @GetMapping
    public MeusDadosResposta consultar(@AuthenticationPrincipal Jwt token) {
        return meusDados.consultar(Long.valueOf(token.getSubject()));
    }

    @PutMapping
    public MeusDadosResposta alterar(@AuthenticationPrincipal Jwt token,
            @Valid @RequestBody MeusDadosRequisicao requisicao) {
        return meusDados.alterar(Long.valueOf(token.getSubject()), requisicao);
    }
}
