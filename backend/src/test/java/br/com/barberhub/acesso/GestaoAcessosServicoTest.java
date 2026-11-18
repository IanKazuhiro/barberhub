package br.com.barberhub.acesso;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class GestaoAcessosServicoTest {

    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final GestaoAcessosServico servico = new GestaoAcessosServico(usuarios, auditoria);

    private static Usuario usuario(long id, String email, Perfil perfil, boolean ativo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Pessoa " + id);
        u.setEmail(email);
        u.setPerfil(perfil);
        u.setAtivo(ativo);
        return u;
    }

    @Test
    void inativaBarbeiroERegistraAuditoria() {
        Usuario barbeiro = usuario(7L, "b@barber.com", Perfil.BARBEIRO, true);
        when(usuarios.findById(7L)).thenReturn(Optional.of(barbeiro));

        UsuarioAdminResposta resposta = servico.alterarSituacao(1L, 7L, false);

        assertFalse(resposta.ativo());
        verify(auditoria).registrar(1L, "INATIVAR_USUARIO", "usuario", 7L);
    }

    @Test
    void reativaUsuarioInativo() {
        Usuario barbeiro = usuario(7L, "b@barber.com", Perfil.BARBEIRO, false);
        when(usuarios.findById(7L)).thenReturn(Optional.of(barbeiro));

        UsuarioAdminResposta resposta = servico.alterarSituacao(1L, 7L, true);

        assertTrue(resposta.ativo());
        verify(auditoria).registrar(1L, "ATIVAR_USUARIO", "usuario", 7L);
    }

    @Test
    void naoInativaAPropriaConta() {
        Usuario admin = usuario(1L, "outro@barber.com", Perfil.ADMINISTRADOR, true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(ExcecaoNegocio.class, () -> servico.alterarSituacao(1L, 1L, false));
        assertTrue(admin.isAtivo());
    }

    @Test
    void naoInativaAdministradorInicial() {
        Usuario inicial = usuario(2L, "admin@barberhub.local", Perfil.ADMINISTRADOR, true);
        when(usuarios.findById(2L)).thenReturn(Optional.of(inicial));

        assertThrows(ExcecaoNegocio.class, () -> servico.alterarSituacao(1L, 2L, false));
        assertTrue(inicial.isAtivo());
    }

    @Test
    void usuarioInexistenteDaNaoEncontrado() {
        when(usuarios.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.alterarSituacao(1L, 99L, false));
    }
}