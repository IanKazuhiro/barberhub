package br.com.barberhub.catalogo;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Catálogo visto pelo cliente: só o que está ativo (RF-CAT-02). */
@Service
@RequiredArgsConstructor
public class CatalogoServico {

    private final ServicoRepositorio servicos;
    private final ComboItemRepositorio itensDeCombo;

    @Transactional(readOnly = true)
    public List<ItemCatalogoResposta> listar() {
        List<Servico> ativos = servicos.findByAtivoOrderByNomeAsc(true);
        Map<Long, List<String>> nomesPorCombo = nomesDosItens(ativos);
        return ativos.stream()
                .map(s -> ItemCatalogoResposta.de(s, nomesPorCombo.getOrDefault(s.getId(), List.of())))
                .toList();
    }

    /** Uma consulta para todos os combos, em vez de uma por combo. */
    private Map<Long, List<String>> nomesDosItens(List<Servico> ativos) {
        List<Long> combos = ativos.stream().filter(Servico::isEhCombo).map(Servico::getId).toList();
        if (combos.isEmpty()) {
            return Map.of();
        }
        List<ComboItem> itens = itensDeCombo.findById_ComboIdIn(combos);
        List<Long> idsDosServicos = itens.stream().map(i -> i.getId().getServicoId()).distinct().toList();
        Map<Long, String> nomePorServico = servicos.findAllById(idsDosServicos).stream()
                .collect(toMap(Servico::getId, Servico::getNome));
        return itens.stream().collect(groupingBy(i -> i.getId().getComboId(),
                mapping(i -> nomePorServico.get(i.getId().getServicoId()), toList())));
    }
}
