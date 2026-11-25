package br.com.barberhub.barbeiro;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final EquipeServico equipe;
    private final EscalaServico escalas;
    private final FolgaServico folgas;

    @GetMapping
    public List<BarbeiroResposta> listar() {
        return equipe.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdminResposta criar(@AuthenticationPrincipal Jwt token,
            @Valid @RequestBody NovoBarbeiroRequisicao requisicao) {
        return barbeiros.criar(idDe(token), requisicao);
    }

    @PutMapping("/{id}")
    public BarbeiroResposta alterar(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
            @Valid @RequestBody AlterarBarbeiroRequisicao requisicao) {
        return equipe.alterar(idDe(token), id, requisicao);
    }

    @GetMapping("/{id}/escala")
    public List<IntervaloEscala> consultarEscala(@PathVariable Long id) {
        return escalas.consultar(id);
    }

    @PutMapping("/{id}/escala")
    public List<IntervaloEscala> substituirEscala(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
            @Valid @RequestBody EscalaRequisicao requisicao) {
        return escalas.substituir(idDe(token), id, requisicao);
    }

    @GetMapping("/{id}/folgas")
    public List<FolgaResposta> listarFolgas(@PathVariable Long id) {
        return folgas.listar(id);
    }

    @PostMapping("/{id}/folgas")
    @ResponseStatus(HttpStatus.CREATED)
    public FolgaResposta criarFolga(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
            @Valid @RequestBody FolgaRequisicao requisicao) {
        return folgas.criar(idDe(token), id, requisicao);
    }

    @DeleteMapping("/{id}/folgas/{folgaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerFolga(@AuthenticationPrincipal Jwt token, @PathVariable Long id, @PathVariable Long folgaId) {
        folgas.remover(idDe(token), id, folgaId);
    }

    private static Long idDe(Jwt token) {
        return Long.valueOf(token.getSubject());
    }
}
