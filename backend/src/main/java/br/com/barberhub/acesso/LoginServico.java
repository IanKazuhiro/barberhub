package br.com.barberhub.acesso;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.compartilhado.erro.AcessoNegadoException;
import br.com.barberhub.compartilhado.erro.NaoAutenticadoException;

@Service
public class LoginServico {

    static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha incorretos.";
    static final String CONTA_INATIVA = "Esta conta está inativa. Procure a barbearia.";
    static final String SESSAO_INVALIDA = "Sua sessão não é mais válida. Entre novamente.";

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder codificador;
    private final TokenServico tokens;
    /** Usado quando o e-mail não existe, para o tempo de resposta não revelar isso. */
    private final String hashFalso;

    public LoginServico(UsuarioRepositorio usuarios, PasswordEncoder codificador, TokenServico tokens) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.tokens = tokens;
        this.hashFalso = codificador.encode(UUID.randomUUID().toString());
    }

    @Transactional(readOnly = true)
    public LoginResposta entrar(LoginRequisicao requisicao) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(requisicao.email().trim()).orElse(null);

        boolean temSenha = usuario != null && usuario.getSenhaHash() != null;
        String hash = temSenha ? usuario.getSenhaHash() : hashFalso;
        boolean senhaConfere = codificador.matches(requisicao.senha(), hash);

        if (!temSenha || !senhaConfere) {
            throw new NaoAutenticadoException(CREDENCIAIS_INVALIDAS);
        }
        if (!usuario.isAtivo()) {
            throw new AcessoNegadoException(CONTA_INATIVA);
        }

        return new LoginResposta(tokens.gerar(usuario), "Bearer", tokens.expiracaoEmSegundos(),
                paraResposta(usuario));
    }

    /** Usuário dono do token. Recusa se a conta não existe mais ou foi inativada. */
    @Transactional(readOnly = true)
    public UsuarioResposta usuarioAtual(Long id) {
        return usuarios.findById(id)
                .filter(Usuario::isAtivo)
                .map(this::paraResposta)
                .orElseThrow(() -> new NaoAutenticadoException(SESSAO_INVALIDA));
    }

    private UsuarioResposta paraResposta(Usuario usuario) {
        return new UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getTelefone(), usuario.getPerfil());
    }
}