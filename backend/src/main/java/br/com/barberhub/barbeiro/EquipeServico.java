package br.com.barberhub.barbeiro;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/** Consulta e edição dos barbeiros (RF-FUN-01). Inativar e reativar continuam no UC03. */
@Service
@RequiredArgsConstructor
public class EquipeServico {

    static final String NAO_ENCONTRADO = "Barbeiro não encontrado.";

    private final UsuarioRepositorio usuarios;
    private final BarbeiroRepositorio barbeiros;
    private final EscalaBarbeiroRepositorio escalas;
    private final AuditoriaServico auditoria;

    @Transactional(readOnly = true)
    public List<BarbeiroResposta> listar() {
        Map<Long, Barbeiro> porId = barbeiros.findAll().stream()
                .collect(Collectors.toMap(Barbeiro::getUsuarioId, Function.identity()));
        Set<Long> comEscala = new HashSet<>(escalas.idsDosBarbeirosComEscala());
        return usuarios.findByPerfilOrderByNomeAsc(Perfil.BARBEIRO).stream()
                .filter(u -> porId.containsKey(u.getId()))
                .map(u -> BarbeiroResposta.de(u, porId.get(u.getId()), comEscala.contains(u.getId())))
                .toList();
    }

    @Transactional
    public BarbeiroResposta alterar(Long administradorId, Long id, AlterarBarbeiroRequisicao requisicao) {
        Barbeiro barbeiro = barbeiros.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));

        usuario.setNome(requisicao.nome().trim());
        usuario.setTelefone(requisicao.telefone());
        usuarios.save(usuario);
        barbeiro.setPercentualComissao(requisicao.percentualComissao());
        barbeiros.save(barbeiro);
        auditoria.registrar(administradorId, "ALTERAR_BARBEIRO", "barbeiro", id);

        return BarbeiroResposta.de(usuario, barbeiro, escalas.existsByBarbeiroId(id));
    }
}
