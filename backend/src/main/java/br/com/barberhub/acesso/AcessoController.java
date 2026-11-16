package br.com.barberhub.acesso;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/acesso")
@RequiredArgsConstructor
public class AcessoController {

    private final CadastroServico cadastro;
    private final LoginServico login;

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrar(@Valid @RequestBody CadastroRequisicao requisicao) {
        return cadastro.cadastrar(requisicao);
    }

    @PostMapping("/login")
    public LoginResposta entrar(@Valid @RequestBody LoginRequisicao requisicao) {
        return login.entrar(requisicao);
    }

    /** Quem está logado: o front-end usa para recuperar a sessão e saber o perfil. */
    @GetMapping("/eu")
    public UsuarioResposta eu(@AuthenticationPrincipal Jwt token) {
        return login.usuarioAtual(Long.valueOf(token.getSubject()));
    }
}