package br.com.barberhub.acesso;

import org.springframework.http.HttpStatus;
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

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrar(@Valid @RequestBody CadastroRequisicao requisicao) {
        return cadastro.cadastrar(requisicao);
    }
}