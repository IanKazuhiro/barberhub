package br.com.barberhub.acesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.barberhub.compartilhado.erro.AcessoNegadoException;
import br.com.barberhub.compartilhado.erro.NaoAutenticadoException;

class LoginServicoTest {

    private UsuarioRepositorio usuarios;
    private PasswordEncoder codificador;
    private TokenServico tokens;
    private LoginServico servico;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepositorio.class);
        codificador = mock(PasswordEncoder.class);
        tokens = mock(TokenServico.class);
        when(codificador.encode(anyString())).thenReturn("hash-falso");
        servico = new LoginServico(usuarios, codificador, tokens);
        when(tokens.gerar(org.mockito.ArgumentMatchers.any(Usuario.class))).thenReturn("token-x");
        when(tokens.expiracaoEmSegundos()).thenReturn(3600L);
    }

    private Usuario usuario(boolean ativo, String hash) {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setNome("Ana Souza");
        u.setEmail("ana@teste.com");
        u.setTelefone("92999998888");
        u.setPerfil(Perfil.CLIENTE);
        u.setAtivo(ativo);
        u.setSenhaHash(hash);
        return u;
    }

    @Test
    void credenciaisCorretasRetornamTokenEUsuarioSemSenha() {
        when(usuarios.findByEmailIgnoreCase("ana@teste.com")).thenReturn(Optional.of(usuario(true, "hash-ok")));
        when(codificador.matches("senha1234", "hash-ok")).thenReturn(true);

        LoginResposta resposta = servico.entrar(new LoginRequisicao(" ana@teste.com ", "senha1234"));

        assertThat(resposta.token()).isEqualTo("token-x");
        assertThat(resposta.tipo()).isEqualTo("Bearer");
        assertThat(resposta.expiraEmSegundos()).isEqualTo(3600L);
        assertThat(resposta.usuario().id()).isEqualTo(7L);
        assertThat(resposta.usuario().perfil()).isEqualTo(Perfil.CLIENTE);
    }

    @Test
    void senhaErradaRecusaComMensagemGenerica() {
        when(usuarios.findByEmailIgnoreCase("ana@teste.com")).thenReturn(Optional.of(usuario(true, "hash-ok")));
        when(codificador.matches("errada", "hash-ok")).thenReturn(false);

        assertThatThrownBy(() -> servico.entrar(new LoginRequisicao("ana@teste.com", "errada")))
                .isInstanceOf(NaoAutenticadoException.class)
                .hasMessage(LoginServico.CREDENCIAIS_INVALIDAS);
    }

    @Test
    void emailInexistenteDaMesmaMensagemEAindaConfereUmHash() {
        when(usuarios.findByEmailIgnoreCase("ninguem@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.entrar(new LoginRequisicao("ninguem@teste.com", "qualquer")))
                .isInstanceOf(NaoAutenticadoException.class)
                .hasMessage(LoginServico.CREDENCIAIS_INVALIDAS);
        verify(codificador).matches("qualquer", "hash-falso");
    }

    @Test
    void contaSemSenhaNaoEntra() {
        when(usuarios.findByEmailIgnoreCase("ana@teste.com")).thenReturn(Optional.of(usuario(true, null)));

        assertThatThrownBy(() -> servico.entrar(new LoginRequisicao("ana@teste.com", "senha1234")))
                .isInstanceOf(NaoAutenticadoException.class)
                .hasMessage(LoginServico.CREDENCIAIS_INVALIDAS);
    }

    @Test
    void contaInativaComSenhaCorretaRecebeAcessoNegado() {
        when(usuarios.findByEmailIgnoreCase("ana@teste.com")).thenReturn(Optional.of(usuario(false, "hash-ok")));
        when(codificador.matches("senha1234", "hash-ok")).thenReturn(true);

        assertThatThrownBy(() -> servico.entrar(new LoginRequisicao("ana@teste.com", "senha1234")))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessage(LoginServico.CONTA_INATIVA);
    }

    @Test
    void contaInativaComSenhaErradaNaoRevelaQueEstaInativa() {
        when(usuarios.findByEmailIgnoreCase("ana@teste.com")).thenReturn(Optional.of(usuario(false, "hash-ok")));
        when(codificador.matches("errada", "hash-ok")).thenReturn(false);

        assertThatThrownBy(() -> servico.entrar(new LoginRequisicao("ana@teste.com", "errada")))
                .isInstanceOf(NaoAutenticadoException.class)
                .hasMessage(LoginServico.CREDENCIAIS_INVALIDAS);
    }

    @Test
    void usuarioAtualDevolveContaAtiva() {
        when(usuarios.findById(7L)).thenReturn(Optional.of(usuario(true, "hash-ok")));

        assertThat(servico.usuarioAtual(7L).email()).isEqualTo("ana@teste.com");
    }

    @Test
    void usuarioAtualRecusaContaInativaOuInexistente() {
        when(usuarios.findById(7L)).thenReturn(Optional.of(usuario(false, "hash-ok")));
        when(usuarios.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.usuarioAtual(7L)).isInstanceOf(NaoAutenticadoException.class);
        assertThatThrownBy(() -> servico.usuarioAtual(8L)).isInstanceOf(NaoAutenticadoException.class);
    }
}