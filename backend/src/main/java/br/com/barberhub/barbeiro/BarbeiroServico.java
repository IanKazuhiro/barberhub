package br.com.barberhub.barbeiro;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioAdminResposta;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BarbeiroServico {

    static final String EMAIL_REPETIDO = "Já existe um usuário com este e-mail.";

    private final UsuarioRepositorio usuarios;
    private final BarbeiroRepositorio barbeiros;
    private final PasswordEncoder codificador;
    private final AuditoriaServico auditoria;

    @Transactional
    public UsuarioAdminResposta criar(Long administradorId, NovoBarbeiroRequisicao requisicao) {
        String email = requisicao.email().trim();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ExcecaoNegocio(EMAIL_REPETIDO);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(requisicao.nome().trim());
        usuario.setEmail(email);
        usuario.setTelefone(requisicao.telefone());
        usuario.setSenhaHash(codificador.encode(requisicao.senha()));
        usuario.setPerfil(Perfil.BARBEIRO);

        try {
            usuario = usuarios.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new ExcecaoNegocio(EMAIL_REPETIDO);
        }

        BigDecimal comissao = requisicao.percentualComissao() != null
                ? requisicao.percentualComissao()
                : BigDecimal.ZERO;
        barbeiros.save(new Barbeiro(usuario.getId(), comissao));
        auditoria.registrar(administradorId, "CRIAR_BARBEIRO", "usuario", usuario.getId());

        return UsuarioAdminResposta.de(usuario);
    }
}