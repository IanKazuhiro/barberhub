package br.com.barberhub.cliente;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Clientes para quem trabalha na barbearia: /api/clientes/** é do administrador e do barbeiro. */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteServico clientes;

    @GetMapping
    public List<ClienteResumoResposta> buscar(@RequestParam(required = false) String busca) {
        return clientes.buscar(busca);
    }

    @GetMapping("/{id}")
    public ClienteDetalheResposta detalhar(@PathVariable Long id) {
        return clientes.detalhar(id);
    }

    @PutMapping("/{id}/preferencias")
    public ClienteDetalheResposta alterarPreferencias(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
            @Valid @RequestBody PreferenciasRequisicao requisicao) {
        return clientes.alterarPreferencias(Long.valueOf(token.getSubject()), id, requisicao);
    }
}
