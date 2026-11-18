package br.com.barberhub.barbeiro;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.barberhub.acesso.UsuarioAdminResposta;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Rotas do administrador sobre barbeiros (a regra de perfil está em /api/admin/** na ConfiguracaoSeguranca). */
@RestController
@RequestMapping("/api/admin/barbeiros")
@RequiredArgsConstructor
public class BarbeiroAdminController {

    private final BarbeiroServico barbeiros;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdminResposta criar(@AuthenticationPrincipal Jwt token,
            @Valid @RequestBody NovoBarbeiroRequisicao requisicao) {
        return barbeiros.criar(Long.valueOf(token.getSubject()), requisicao);
    }
}