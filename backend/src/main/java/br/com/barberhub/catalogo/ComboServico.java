package br.com.barberhub.catalogo;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComboServico {

    static final String NOME_REPETIDO = "Já existe um serviço com este nome.";
    static final String NAO_ENCONTRADO = "Combo não encontrado.";
    static final String ITENS_REPETIDOS = "Não repita o mesmo serviço no combo.";
    static final String ITEM_INVALIDO = "Todos os serviços do combo precisam existir, estar ativos e não ser outro combo.";

    private final ServicoRepositorio servicos;
    private final ComboItemRepositorio itensRepositorio;
    private final AuditoriaServico auditoria;
    static final String MINIMO_DOIS_ITENS = "Informe ao menos 2 serviços para o combo.";

    @Transactional(readOnly = true)
    public List<ComboResposta> listar(Boolean ativo) {
        List<Servico> combos = ativo == null
                ? servicos.findByEhComboTrueOrderByNomeAsc()
                : servicos.findByEhComboTrueAndAtivoOrderByNomeAsc(ativo);
        return combos.stream().map(this::paraResposta).toList();
    }

    @Transactional
    public ComboResposta criar(Long administradorId, ComboRequisicao requisicao) {
        String nome = requisicao.nome().trim();
        if (servicos.existsByNomeIgnoreCase(nome)) {
            throw new ExcecaoNegocio(NOME_REPETIDO);
        }
        List<Servico> itens = validarItens(requisicao.servicoIds());

        Servico combo = new Servico();
        combo.setNome(nome);
        combo.setPreco(requisicao.preco());
        combo.setEhCombo(true);
        combo.setDuracaoMinutos(duracaoTotal(itens));
        combo = salvar(combo);

        salvarItens(combo.getId(), itens);
        auditoria.registrar(administradorId, "CRIAR_COMBO", "servico", combo.getId());
        return ComboResposta.de(combo, itens);
    }

    @Transactional
    public ComboResposta alterar(Long administradorId, Long id, ComboRequisicao requisicao) {
        Servico combo = buscarCombo(id);
        String nome = requisicao.nome().trim();
        if (servicos.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ExcecaoNegocio(NOME_REPETIDO);
        }
        List<Servico> itens = validarItens(requisicao.servicoIds());

        combo.setNome(nome);
        combo.setPreco(requisicao.preco());
        combo.setDuracaoMinutos(duracaoTotal(itens));
        combo = salvar(combo);

        itensRepositorio.deleteByComboId(id);
        itensRepositorio.flush();
        salvarItens(id, itens);

        auditoria.registrar(administradorId, "ALTERAR_COMBO", "servico", combo.getId());
        return ComboResposta.de(combo, itens);
    }

    private ComboResposta paraResposta(Servico combo) {
        List<Long> idsDosItens = itensRepositorio.findById_ComboId(combo.getId()).stream()
                .map(i -> i.getId().getServicoId())
                .toList();
        List<Servico> itens = servicos.findAllById(idsDosItens);
        return ComboResposta.de(combo, itens);
    }

    private Servico buscarCombo(Long id) {
        Servico servico = servicos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));
        if (!servico.isEhCombo()) {
            throw new RecursoNaoEncontradoException(NAO_ENCONTRADO);
        }
        return servico;
    }

    private List<Servico> validarItens(List<Long> servicoIds) {
        if (servicoIds.size() < 2) {
            throw new ExcecaoNegocio(MINIMO_DOIS_ITENS);
        }
        if (servicoIds.stream().distinct().count() != servicoIds.size()) {
            throw new ExcecaoNegocio(ITENS_REPETIDOS);
        }
        List<Servico> itens = servicos.findAllById(servicoIds);
        boolean todosValidos = itens.size() == servicoIds.size()
                && itens.stream().allMatch(s -> s.isAtivo() && !s.isEhCombo());
        if (!todosValidos) {
            throw new ExcecaoNegocio(ITEM_INVALIDO);
        }
        return itens;
    }

    private short duracaoTotal(List<Servico> itens) {
        int soma = itens.stream().mapToInt(Servico::getDuracaoMinutos).sum();
        return (short) soma;
    }

    private void salvarItens(Long comboId, List<Servico> itens) {
        for (Servico item : itens) {
            itensRepositorio.save(new ComboItem(comboId, item.getId()));
        }
    }

    private Servico salvar(Servico servico) {
        try {
            return servicos.saveAndFlush(servico);
        } catch (DataIntegrityViolationException e) {
            throw new ExcecaoNegocio(NOME_REPETIDO);
        }
    }
}