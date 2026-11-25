package br.com.barberhub.cliente;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Só o administrador altera os dados de qualquer cliente (RF-CLI-02). */
@RestController
@RequestMapping("/api/admin/clientes")
@RequiredArgsConstructor
public class ClienteAdminController {

    private final ClienteServico clientes;

    @PutMapping("/{id}")
    public ClienteDetalheResposta alterarDados(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
            @Valid @RequestBody DadosClienteRequisicao requisicao) {
        return clientes.alterarDados(Long.valueOf(token.getSubject()), id, requisicao);
    }
}
