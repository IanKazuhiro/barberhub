package br.com.barberhub.agendamento;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/barbeiros/{barbeiroId}/agendamentos-futuros")
@RequiredArgsConstructor
public class AgendamentosFuturosController {

    private final AgendamentosFuturosServico futuros;

    @GetMapping
    public List<AgendamentoFuturoResposta> listar(@PathVariable Long barbeiroId) {
        return futuros.doBarbeiro(barbeiroId);
    }
}
