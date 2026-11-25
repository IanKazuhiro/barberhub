package br.com.barberhub.barbeiro;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** Barbeiros ativos, para qualquer usuário logado. */
@RestController
@RequestMapping("/api/barbeiros")
@RequiredArgsConstructor
public class BarbeiroController {

    private final EquipeServico equipe;

    @GetMapping
    public List<BarbeiroAtivoResposta> listarAtivos() {
        return equipe.listarAtivos();
    }
}
