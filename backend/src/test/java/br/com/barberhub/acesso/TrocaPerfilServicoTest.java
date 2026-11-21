package br.com.barberhub.acesso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.barberhub.barbeiro.Barbeiro;
import br.com.barberhub.barbeiro.BarbeiroRepositorio;
import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class TrocaPerfilServicoTest {

    private static final long ADMIN = 1L;
    private static final long ALVO = 5L;

    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final ClienteRepositorio clientes = mock(ClienteRepositorio.class);
    private final BarbeiroRepositorio barbeiros = mock(BarbeiroRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final TrocaPerfilServico servico =
            new TrocaPerfilServico(usuarios, clientes, barbeiros, auditoria);

    private Usuario alvo(String email, Perfil perfil) {
        Usuario u = new Usuario();
        u.setId(ALVO);
        u.setNome("Rafael Souza");
        u.setEmail(email);
        u.setPerfil(perfil);
        when(usuarios.findById(ALVO)).thenReturn(Optional.of(u));
        return u;
    }

    @Test
    void clienteVirandoBarbeiroTrocaALinhaERegistraAuditoria() {
        alvo("rafael@teste.com", Perfil.CLIENTE);
        when(clientes.existsById(ALVO)).thenReturn(true);

        UsuarioAdminResposta resposta = servico.trocar(ADMIN, ALVO, Perfil.BARBEIRO);

        assertEquals(Perfil.BARBEIRO, resposta.perfil());
        verify(clientes).deleteById(ALVO);
        ArgumentCaptor<Barbeiro> criado = ArgumentCaptor.forClass(Barbeiro.class);
        verify(barbeiros).save(criado.capture());
        assertEquals(ALVO, criado.getValue().getUsuarioId());
        assertEquals(0, BigDecimal.ZERO.compareTo(criado.getValue().getPercentualComissao()));
        verify(auditoria).registrar(ADMIN, "TROCAR_PERFIL_BARBEIRO", "usuario", ALVO);
    }

    @Test
    void barbeiroVirandoClienteTrocaALinha() {
        alvo("rafael@teste.com", Perfil.BARBEIRO);
        when(barbeiros.existsById(ALVO)).thenReturn(true);

        UsuarioAdminResposta resposta = servico.trocar(ADMIN, ALVO, Perfil.CLIENTE);

        assertEquals(Perfil.CLIENTE, resposta.perfil());
        verify(barbeiros).deleteById(ALVO);
        verify(clientes).save(any(Cliente.class));
        verify(auditoria).registrar(ADMIN, "TROCAR_PERFIL_CLIENTE", "usuario", ALVO);
    }

    @Test
    void virandoAdministradorRemoveAsLinhasEnaoCriaNenhuma() {
        alvo("rafael@teste.com", Perfil.CLIENTE);
        when(clientes.existsById(ALVO)).thenReturn(true);

        servico.trocar(ADMIN, ALVO, Perfil.ADMINISTRADOR);

        verify(clientes).deleteById(ALVO);
        verify(clientes, never()).save(any());
        verify(barbeiros, never()).save(any());
        verify(auditoria).registrar(ADMIN, "TROCAR_PERFIL_ADMINISTRADOR", "usuario", ALVO);
    }

    @Test
    void mesmoPerfilNaoFazNadaENaoAudita() {
        alvo("rafael@teste.com", Perfil.BARBEIRO);

        UsuarioAdminResposta resposta = servico.trocar(ADMIN, ALVO, Perfil.BARBEIRO);

        assertEquals(Perfil.BARBEIRO, resposta.perfil());
        verify(clientes, never()).deleteById(any());
        verify(barbeiros, never()).save(any());
        verify(auditoria, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void naoTrocaOProprioPerfil() {
        Usuario eu = alvo("outro@barber.com", Perfil.ADMINISTRADOR);

        assertThrows(ExcecaoNegocio.class, () -> servico.trocar(ALVO, ALVO, Perfil.CLIENTE));
        assertEquals(Perfil.ADMINISTRADOR, eu.getPerfil());
    }

    @Test
    void naoTrocaOPerfilDoAdministradorInicial() {
        Usuario inicial = alvo(Usuario.EMAIL_ADMINISTRADOR_INICIAL, Perfil.ADMINISTRADOR);

        assertThrows(ExcecaoNegocio.class, () -> servico.trocar(ADMIN, ALVO, Perfil.CLIENTE));
        assertEquals(Perfil.ADMINISTRADOR, inicial.getPerfil());
    }

    @Test
    void usuarioInexistenteDaNaoEncontrado() {
        when(usuarios.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.trocar(ADMIN, 99L, Perfil.CLIENTE));
    }

    @Test
    void recusaQuandoOBancoImpedeARemocaoPorHistorico() {
        Usuario usuario = alvo("rafael@teste.com", Perfil.CLIENTE);
        when(clientes.existsById(ALVO)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(clientes).flush();

        ExcecaoNegocio erro = assertThrows(ExcecaoNegocio.class,
                () -> servico.trocar(ADMIN, ALVO, Perfil.BARBEIRO));

        assertEquals(TrocaPerfilServico.COM_HISTORICO, erro.getMessage());
        assertEquals(Perfil.CLIENTE, usuario.getPerfil());
        verify(barbeiros, never()).save(any());
        verify(auditoria, never()).registrar(any(), any(), any(), any());
    }
}
