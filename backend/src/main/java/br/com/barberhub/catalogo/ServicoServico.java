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
public class ServicoServico {

    static final String NOME_REPETIDO = "Já existe um serviço com este nome.";
    static final String NAO_ENCONTRADO = "Serviço não encontrado.";
    private final ServicoRepositorio servicos;
    private final AuditoriaServico auditoria;

    @Transactional(readOnly = true)
    public List<ServicoResposta> listar(Boolean ativo) {
        List<Servico> lista = ativo == null
                ? servicos.findAllByOrderByNomeAsc()
                : servicos.findByAtivoOrderByNomeAsc(ativo);
        return lista.stream().map(ServicoResposta::de).toList();
    }

    @Transactional
    public ServicoResposta criar(Long administradorId, ServicoRequisicao requisicao) {
        String nome = requisicao.nome().trim();
        if (servicos.existsByNomeIgnoreCase(nome)) {
            throw new ExcecaoNegocio(NOME_REPETIDO);
        }

        Servico servico = new Servico();
        preencher(servico, nome, requisicao);
        servico = salvar(servico);
        auditoria.registrar(administradorId, "CRIAR_SERVICO", "servico", servico.getId());
        return ServicoResposta.de(servico);
    }

    @Transactional
    public ServicoResposta alterar(Long administradorId, Long id, ServicoRequisicao requisicao) {
        Servico servico = buscar(id);
        String nome = requisicao.nome().trim();
        if (servicos.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ExcecaoNegocio(NOME_REPETIDO);
        }

        preencher(servico, nome, requisicao);
        servico = salvar(servico);
        auditoria.registrar(administradorId, "ALTERAR_SERVICO", "servico", servico.getId());
        return ServicoResposta.de(servico);
    }

    @Transactional
    public ServicoResposta alterarSituacao(Long administradorId, Long id, boolean ativo) {
        Servico servico = buscar(id);
        if (servico.isAtivo() != ativo) {
            servico.setAtivo(ativo);
            servicos.save(servico);
            auditoria.registrar(administradorId, ativo ? "ATIVAR_SERVICO" : "INATIVAR_SERVICO",
                    "servico", servico.getId());
        }
        return ServicoResposta.de(servico);
    }

    private Servico buscar(Long id) {
        return servicos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));
    }

    private void preencher(Servico servico, String nome, ServicoRequisicao requisicao) {
        servico.setNome(nome);
        servico.setDuracaoMinutos(requisicao.duracaoMinutos().shortValue());
        servico.setPreco(requisicao.preco());
    }

    private Servico salvar(Servico servico) {
        try {
            return servicos.saveAndFlush(servico);
        } catch (DataIntegrityViolationException e) {
            throw new ExcecaoNegocio(NOME_REPETIDO);
        }
    }
}