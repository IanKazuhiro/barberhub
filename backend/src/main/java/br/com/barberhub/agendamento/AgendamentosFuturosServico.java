package br.com.barberhub.agendamento;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.barbeiro.BarbeiroRepositorio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/** O que ficou marcado com um barbeiro, para o administrador agir quando ele é inativado (RF-FUN-03). */
@Service
@RequiredArgsConstructor
public class AgendamentosFuturosServico {

    static final String BARBEIRO_NAO_ENCONTRADO = "Barbeiro não encontrado.";

    private final BarbeiroRepositorio barbeiros;
    private final AgendamentoRepositorio agendamentos;
    private final DisponibilidadeServico disponibilidade;
    private final Clock relogio;

    @Transactional(readOnly = true)
    public List<AgendamentoFuturoResposta> doBarbeiro(Long barbeiroId) {
        if (!barbeiros.existsById(barbeiroId)) {
            throw new RecursoNaoEncontradoException(BARBEIRO_NAO_ENCONTRADO);
        }
        return agendamentos.emAbertoDoBarbeiro(barbeiroId, relogio.instant()).stream()
                .map(linha -> new AgendamentoFuturoResposta(linha.agendamento().getId(),
                        disponibilidade.local(linha.agendamento().getInicio()),
                        disponibilidade.local(linha.agendamento().getFim()),
                        linha.cliente(), linha.telefone(), linha.servico()))
                .toList();
    }
}
