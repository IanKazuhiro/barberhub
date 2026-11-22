package br.com.barberhub.acesso;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Rotas do administrador sobre qualquer usuário (a regra está em /api/admin/** na ConfiguracaoSeguranca). */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminAcessosController {

    private final GestaoAcessosServico acessos;
    private final TrocaPerfilServico perfis;

    @GetMapping
    public List<UsuarioAdminResposta> listar(@RequestParam(required = false) Perfil perfil,
            @RequestParam(required = false) Boolean ativo) {
        return acessos.listar(perfil, ativo);
    }

    @PatchMapping("/{id}/inativar")
    public UsuarioAdminResposta inativar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return acessos.alterarSituacao(idDe(token), id, false);
    }

    @PatchMapping("/{id}/ativar")
    public UsuarioAdminResposta ativar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return acessos.alterarSituacao(idDe(token), id, true);
    }

    @PatchMapping("/{id}/perfil")
    public UsuarioAdminResposta trocarPerfil(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
            @Valid @RequestBody TrocaPerfilRequisicao requisicao) {
        return perfis.trocar(idDe(token), id, requisicao.perfil());
    }

    private static Long idDe(Jwt token) {
        return Long.valueOf(token.getSubject());
    }
}