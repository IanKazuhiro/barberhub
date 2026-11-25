package br.com.barberhub.catalogo;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** Catálogo do cliente (a regra de perfil está em /api/cliente/** na ConfiguracaoSeguranca). */
@RestController
@RequestMapping("/api/cliente/catalogo")
@RequiredArgsConstructor
public class CatalogoClienteController {

    private final CatalogoServico catalogo;

    @GetMapping
    public List<ItemCatalogoResposta> listar() {
        return catalogo.listar();
    }
}
