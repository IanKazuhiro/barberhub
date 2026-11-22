package br.com.barberhub.acesso;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestaoAcessosServico {

    static final String NAO_INATIVA_A_SI_MESMO = "Você não pode inativar a sua própria conta.";
    static final String NAO_INATIVA_ADMIN_INICIAL = "A conta do administrador inicial não pode ser inativada.";
    static final String NAO_ENCONTRADO = "Usuário não encontrado.";

    private final UsuarioRepositorio usuarios;
    private final AuditoriaServico auditoria;

    @Transactional(readOnly = true)
    public List<UsuarioAdminResposta> listar(Perfil perfil, Boolean ativo) {
        return usuarios.findAll().stream()
                .filter(u -> perfil == null || u.getPerfil() == perfil)
                .filter(u -> ativo == null || u.isAtivo() == ativo)
                .sorted(Comparator.comparing(Usuario::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(UsuarioAdminResposta::de)
                .toList();
    }

    @Transactional
    public UsuarioAdminResposta alterarSituacao(Long administradorId, Long id, boolean ativo) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));

        if (!ativo) {
            if (usuario.getId().equals(administradorId)) {
                throw new ExcecaoNegocio(NAO_INATIVA_A_SI_MESMO);
            }
            if (Usuario.EMAIL_ADMINISTRADOR_INICIAL.equalsIgnoreCase(usuario.getEmail())) {
                throw new ExcecaoNegocio(NAO_INATIVA_ADMIN_INICIAL);
            }
        }

        if (usuario.isAtivo() != ativo) {
            usuario.setAtivo(ativo);
            usuarios.save(usuario);
            auditoria.registrar(administradorId, ativo ? "ATIVAR_USUARIO" : "INATIVAR_USUARIO",
                    "usuario", usuario.getId());
        }
        return UsuarioAdminResposta.de(usuario);
    }
}