package br.com.barberhub.barbeiro;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/** Escala semanal do barbeiro (RF-FUN-02). */
@Service
@RequiredArgsConstructor
public class EscalaServico {

    static final String INICIO_ANTES_DO_FIM = "Em cada intervalo, o início precisa ser antes do fim.";
    static final String INTERVALOS_SOBREPOSTOS = "Há intervalos que se sobrepõem no mesmo dia.";

    private static final Comparator<IntervaloEscala> POR_DIA_E_INICIO =
            Comparator.comparing(IntervaloEscala::diaSemana).thenComparing(IntervaloEscala::inicio);

    private final BarbeiroRepositorio barbeiros;
    private final EscalaBarbeiroRepositorio escalas;
    private final AuditoriaServico auditoria;

    @Transactional(readOnly = true)
    public List<IntervaloEscala> consultar(Long barbeiroId) {
        exigirBarbeiro(barbeiroId);
        return escalas.findByBarbeiroIdOrderByDiaSemanaAscInicioAsc(barbeiroId).stream()
                .map(IntervaloEscala::de)
                .toList();
    }

    @Transactional
    public List<IntervaloEscala> substituir(Long administradorId, Long barbeiroId, EscalaRequisicao requisicao) {
        exigirBarbeiro(barbeiroId);
        List<IntervaloEscala> intervalos = requisicao.intervalos().stream().sorted(POR_DIA_E_INICIO).toList();
        validar(intervalos);

        escalas.apagarDoBarbeiro(barbeiroId);
        escalas.saveAll(intervalos.stream()
                .map(i -> new EscalaBarbeiro(barbeiroId, i.diaSemana(), i.inicio(), i.fim()))
                .toList());
        auditoria.registrar(administradorId, "ALTERAR_ESCALA", "barbeiro", barbeiroId);
        return intervalos;
    }

    /** Recebe os intervalos em ordem: basta comparar cada um com o anterior. Encostar um no outro é permitido. */
    private static void validar(List<IntervaloEscala> intervalos) {
        IntervaloEscala anterior = null;
        for (IntervaloEscala atual : intervalos) {
            if (!atual.inicio().isBefore(atual.fim())) {
                throw new ExcecaoNegocio(INICIO_ANTES_DO_FIM);
            }
            if (anterior != null && anterior.diaSemana().equals(atual.diaSemana())
                    && atual.inicio().isBefore(anterior.fim())) {
                throw new ExcecaoNegocio(INTERVALOS_SOBREPOSTOS);
            }
            anterior = atual;
        }
    }

    private void exigirBarbeiro(Long barbeiroId) {
        if (!barbeiros.existsById(barbeiroId)) {
            throw new RecursoNaoEncontradoException(EquipeServico.NAO_ENCONTRADO);
        }
    }
}
