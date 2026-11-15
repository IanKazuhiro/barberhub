package br.com.barberhub.acesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;

class CadastroServicoTest {

    private UsuarioRepositorio usuarios;
    private ClienteRepositorio clientes;
    private PasswordEncoder codificador;
    private CadastroServico servico;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepositorio.class);
        clientes = mock(ClienteRepositorio.class);
        codificador = mock(PasswordEncoder.class);
        servico = new CadastroServico(usuarios, clientes, codificador);
        when(codificador.encode("senha1234")).thenReturn("hash-da-senha");
    }

    private CadastroRequisicao requisicao() {
        return new CadastroRequisicao("  Ana Souza ", " ana@teste.com ", "92999998888", "senha1234");
    }

    @Test
    void emailNovoCriaContaDeClienteComSenhaCodificada() {
        when(usuarios.existsByEmailIgnoreCase("ana@teste.com")).thenReturn(false);
        when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(7L);
            return u;
        });

        UsuarioResposta resposta = servico.cadastrar(requisicao());

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).saveAndFlush(salvo.capture());
        assertThat(salvo.getValue().getSenhaHash()).isEqualTo("hash-da-senha");
        assertThat(salvo.getValue().getNome()).isEqualTo("Ana Souza");
        assertThat(salvo.getValue().getEmail()).isEqualTo("ana@teste.com");
        assertThat(salvo.getValue().getPerfil()).isEqualTo(Perfil.CLIENTE);

        ArgumentCaptor<Cliente> cliente = ArgumentCaptor.forClass(Cliente.class);
        verify(clientes).save(cliente.capture());
        assertThat(cliente.getValue().getUsuarioId()).isEqualTo(7L);

        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.perfil()).isEqualTo(Perfil.CLIENTE);
    }

    @Test
    void emailRepetidoEhRecusadoSemSalvarNada() {
        when(usuarios.existsByEmailIgnoreCase("ana@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> servico.cadastrar(requisicao()))
                .isInstanceOf(ExcecaoNegocio.class)
                .hasMessage(CadastroServico.EMAIL_REPETIDO);

        verify(usuarios, never()).saveAndFlush(any());
        verify(clientes, never()).save(any());
    }

    @Test
    void cadastroSimultaneoComMesmoEmailEhRecusadoPeloBanco() {
        when(usuarios.existsByEmailIgnoreCase("ana@teste.com")).thenReturn(false);
        when(usuarios.saveAndFlush(any(Usuario.class))).thenThrow(new DataIntegrityViolationException("uq_usuario_email"));

        assertThatThrownBy(() -> servico.cadastrar(requisicao()))
                .isInstanceOf(ExcecaoNegocio.class)
                .hasMessage(CadastroServico.EMAIL_REPETIDO);

        verify(clientes, never()).save(any());
    }
}