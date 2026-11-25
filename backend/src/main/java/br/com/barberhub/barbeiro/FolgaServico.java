package br.com.barberhub.barbeiro;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/** Folgas do barbeiro: dias inteiros sem atendimento (RF-FUN-02). */
@Service
@RequiredArgsConstructor
public class FolgaServico {

    static final String NO_PASSADO = "A folga não pode ser em um dia que já passou.";
    static final String REPETIDA = "Este barbeiro já tem folga nesse dia.";
    static final String NAO_ENCONTRADA = "Folga não encontrada.";

    private final BarbeiroRepositorio barbeiros;
    private final FolgaBarbeiroRepositorio folgas;
    private final AuditoriaServico auditoria;
    private final Clock relogio;

    /** Só as de hoje em diante: as que passaram não mudam mais nada na agenda. */
    @Transactional(readOnly = true)
    public List<FolgaResposta> listar(Long barbeiroId) {
        exigirBarbeiro(barbeiroId);
        return folgas.findByBarbeiroIdAndDataGreaterThanEqualOrderByDataAsc(barbeiroId, LocalDate.now(relogio))
                .stream()
                .map(FolgaResposta::de)
                .toList();
    }

    @Transactional
    public FolgaResposta criar(Long administradorId, Long barbeiroId, FolgaRequisicao requisicao) {
        exigirBarbeiro(barbeiroId);
        if (requisicao.data().isBefore(LocalDate.now(relogio))) {
            throw new ExcecaoNegocio(NO_PASSADO);
        }
        if (folgas.existsByBarbeiroIdAndData(barbeiroId, requisicao.data())) {
            throw new ExcecaoNegocio(REPETIDA);
        }

        FolgaBarbeiro folga;
        try {
            folga = folgas.saveAndFlush(
                    new FolgaBarbeiro(barbeiroId, requisicao.data(), textoOuNulo(requisicao.motivo())));
        } catch (DataIntegrityViolationException e) {
            throw new ExcecaoNegocio(REPETIDA);
        }
        auditoria.registrar(administradorId, "CRIAR_FOLGA", "barbeiro", barbeiroId);
        return FolgaResposta.de(folga);
    }

    @Transactional
    public void remover(Long administradorId, Long barbeiroId, Long folgaId) {
        FolgaBarbeiro folga = folgas.findByIdAndBarbeiroId(folgaId, barbeiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADA));
        folgas.delete(folga);
        auditoria.registrar(administradorId, "REMOVER_FOLGA", "barbeiro", barbeiroId);
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private void exigirBarbeiro(Long barbeiroId) {
        if (!barbeiros.existsById(barbeiroId)) {
            throw new RecursoNaoEncontradoException(EquipeServico.NAO_ENCONTRADO);
        }
    }
}
