package br.com.barberhub.barbeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class EquipeServicoTest {

    private static final long ADMIN = 1L;
    private static final long RAFAEL = 5L;

    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final BarbeiroRepositorio barbeiros = mock(BarbeiroRepositorio.class);
    private final EscalaBarbeiroRepositorio escalas = mock(EscalaBarbeiroRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final EquipeServico equipe = new EquipeServico(usuarios, barbeiros, escalas, auditoria);

    private static Usuario usuario(long id, String nome, boolean ativo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail("pessoa" + id + "@teste.com");
        u.setTelefone("92999990000");
        u.setPerfil(Perfil.BARBEIRO);
        u.setAtivo(ativo);
        return u;
    }

    private static AlterarBarbeiroRequisicao dados(String nome, String comissao) {
        return new AlterarBarbeiroRequisicao(nome, "92988887777", new BigDecimal(comissao));
    }

    @Test
    void listaAtivosEInativosComComissaoEEscala() {
        when(usuarios.findByPerfilOrderByNomeAsc(Perfil.BARBEIRO))
                .thenReturn(List.of(usuario(RAFAEL, "Rafael Souza", true), usuario(6L, "Tiago Lima", false)));
        when(barbeiros.findAll()).thenReturn(List.of(
                new Barbeiro(RAFAEL, new BigDecimal("40.00")), new Barbeiro(6L, BigDecimal.ZERO)));
        when(escalas.idsDosBarbeirosComEscala()).thenReturn(List.of(RAFAEL));

        List<BarbeiroResposta> lista = equipe.listar();

        assertEquals(2, lista.size());
        assertEquals(new BigDecimal("40.00"), lista.get(0).percentualComissao());
        assertTrue(lista.get(0).possuiEscala());
        assertFalse(lista.get(1).possuiEscala());
        assertFalse(lista.get(1).ativo());
    }

    @Test
    void alteraNomeTelefoneEComissaoERegistraAuditoria() {
        Usuario rafael = usuario(RAFAEL, "Rafael", true);
        Barbeiro barbeiro = new Barbeiro(RAFAEL, BigDecimal.ZERO);
        when(usuarios.findById(RAFAEL)).thenReturn(Optional.of(rafael));
        when(barbeiros.findById(RAFAEL)).thenReturn(Optional.of(barbeiro));

        BarbeiroResposta resposta = equipe.alterar(ADMIN, RAFAEL, dados("  Rafael Souza ", "35.50"));

        assertEquals("Rafael Souza", resposta.nome());
        assertEquals("92988887777", rafael.getTelefone());
        assertEquals(new BigDecimal("35.50"), barbeiro.getPercentualComissao());
        verify(auditoria).registrar(ADMIN, "ALTERAR_BARBEIRO", "barbeiro", RAFAEL);
    }

    @Test
    void alterarQuemNaoEhBarbeiroDaNaoEncontrado() {
        when(barbeiros.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> equipe.alterar(ADMIN, 99L, dados("Fulano", "10")));
        verify(usuarios, never()).save(any());
        verify(auditoria, never()).registrar(any(), any(), any(), any());
    }
}
