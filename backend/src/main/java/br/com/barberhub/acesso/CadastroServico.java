package br.com.barberhub.acesso;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CadastroServico {

    static final String EMAIL_REPETIDO = "Este e-mail já está cadastrado. Use outro e-mail ou entre na sua conta.";

    private final UsuarioRepositorio usuarios;
    private final ClienteRepositorio clientes;
    private final PasswordEncoder codificador;

    @Transactional
    public UsuarioResposta cadastrar(CadastroRequisicao requisicao) {
        String email = requisicao.email().trim();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ExcecaoNegocio(EMAIL_REPETIDO);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(requisicao.nome().trim());
        usuario.setEmail(email);
        usuario.setTelefone(requisicao.telefone());
        usuario.setSenhaHash(codificador.encode(requisicao.senha()));
        usuario.setPerfil(Perfil.CLIENTE);

        try {
            usuario = usuarios.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            // Dois cadastros quase simultâneos com o mesmo e-mail: o banco recusa o segundo.
            throw new ExcecaoNegocio(EMAIL_REPETIDO);
        }
        clientes.save(new Cliente(usuario.getId()));

        return new UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getTelefone(), usuario.getPerfil());
    }
}