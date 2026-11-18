package br.com.barberhub.barbeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioAdminResposta;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;

class BarbeiroServicoTest {

    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final BarbeiroRepositorio barbeiros = mock(BarbeiroRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final BarbeiroServico servico =
            new BarbeiroServico(usuarios, barbeiros, new BCryptPasswordEncoder(), auditoria);

    private static NovoBarbeiroRequisicao novoBarbeiro(BigDecimal comissao) {
        return new NovoBarbeiroRequisicao("Carlos Souza", "carlos@barber.com", "92999990000",
                "senha1234", comissao);
    }

    @Test
    void criaBarbeiroComSenhaEmHashEComissaoZeroPorPadrao() {
        when(usuarios.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(usuarios.saveAndFlush(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(5L);
            return u;
        });

        UsuarioAdminResposta resposta = servico.criar(1L, novoBarbeiro(null));

        assertEquals(Perfil.BARBEIRO, resposta.perfil());
        assertTrue(resposta.ativo());

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).saveAndFlush(salvo.capture());
        assertNotEquals("senha1234", salvo.getValue().getSenhaHash());

        ArgumentCaptor<Barbeiro> barbeiro = ArgumentCaptor.forClass(Barbeiro.class);
        verify(barbeiros).save(barbeiro.capture());
        assertEquals(0, BigDecimal.ZERO.compareTo(barbeiro.getValue().getPercentualComissao()));
        verify(auditoria).registrar(1L, "CRIAR_BARBEIRO", "usuario", 5L);
    }

    @Test
    void recusaEmailRepetido() {
        when(usuarios.existsByEmailIgnoreCase("carlos@barber.com")).thenReturn(true);

        assertThrows(ExcecaoNegocio.class, () -> servico.criar(1L, novoBarbeiro(null)));
        verify(usuarios, never()).saveAndFlush(any());
    }
}