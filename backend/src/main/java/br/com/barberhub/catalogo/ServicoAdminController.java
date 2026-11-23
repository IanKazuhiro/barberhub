package br.com.barberhub.catalogo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Rotas do administrador sobre serviços (a regra de perfil está em /api/admin/** na ConfiguracaoSeguranca). */
@RestController
@RequestMapping("/api/admin/servicos")
@RequiredArgsConstructor
public class ServicoAdminController {

    private final ServicoServico servicos;

    @GetMapping
    public List<ServicoResposta> listar(@RequestParam(required = false) Boolean ativo) {
        return servicos.listar(ativo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResposta criar(@AuthenticationPrincipal Jwt token,
            @Valid @RequestBody ServicoRequisicao requisicao) {
        return servicos.criar(idDe(token), requisicao);
    }

    @PutMapping("/{id}")
    public ServicoResposta alterar(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
            @Valid @RequestBody ServicoRequisicao requisicao) {
        return servicos.alterar(idDe(token), id, requisicao);
    }

    @PatchMapping("/{id}/inativar")
    public ServicoResposta inativar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return servicos.alterarSituacao(idDe(token), id, false);
    }

    @PatchMapping("/{id}/ativar")
    public ServicoResposta ativar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return servicos.alterarSituacao(idDe(token), id, true);
    }

    private static Long idDe(Jwt token) {
        return Long.valueOf(token.getSubject());
    }
}