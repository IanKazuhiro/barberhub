package br.com.barberhub.catalogo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
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

@RestController
@RequestMapping("/api/admin/combos")
@RequiredArgsConstructor
public class ComboAdminController {

    private final ComboServico combos;

    @GetMapping
    public List<ComboResposta> listar(@RequestParam(required = false) Boolean ativo) {
        return combos.listar(ativo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComboResposta criar(@AuthenticationPrincipal Jwt token,
            @Valid @RequestBody ComboRequisicao requisicao) {
        return combos.criar(idDe(token), requisicao);
    }

    @PutMapping("/{id}")
    public ComboResposta alterar(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
            @Valid @RequestBody ComboRequisicao requisicao) {
        return combos.alterar(idDe(token), id, requisicao);
    }

    private static Long idDe(Jwt token) {
        return Long.valueOf(token.getSubject());
    }
}