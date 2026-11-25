package br.com.barberhub.agendamento;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** Histórico do cliente para o administrador e o barbeiro (a regra está em /api/clientes/**). */
@RestController
@RequestMapping("/api/clientes/{clienteId}/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoServico historico;

    @GetMapping
    public List<AtendimentoResposta> listar(@PathVariable Long clienteId) {
        return historico.doCliente(clienteId);
    }
}
