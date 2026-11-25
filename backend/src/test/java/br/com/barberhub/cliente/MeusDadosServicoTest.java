package br.com.barberhub.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.barberhub.acesso.Cliente;
import br.com.barberhub.acesso.ClienteRepositorio;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;

class MeusDadosServicoTest {

    private static final long CARLA = 2L;
    private static final Instant AGORA = Instant.parse("2026-12-01T15:00:00Z");

    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final ClienteRepositorio clientes = mock(ClienteRepositorio.class);
    private final MeusDadosServico servico =
            new MeusDadosServico(usuarios, clientes, Clock.fixed(AGORA, ZoneOffset.UTC));

    private final Usuario usuario = new Usuario();
    private final Cliente cliente = new Cliente(CARLA);

    @BeforeEach
    void preparar() {
        usuario.setId(CARLA);
        usuario.setNome("Carla Mendes");
        usuario.setTelefone("92988887777");
        when(usuarios.findById(CARLA)).thenReturn(Optional.of(usuario));
        when(clientes.findById(CARLA)).thenReturn(Optional.of(cliente));
    }

    @Test
    void alteraNomeETelefone() {
        MeusDadosResposta resposta = servico.alterar(CARLA, new MeusDadosRequisicao(" Carla Lima ", "92977776666", true));

        assertEquals("Carla Lima", resposta.nome());
        assertEquals("92977776666", usuario.getTelefone());
        verify(usuarios).save(usuario);
    }

    @Test
    void optarPorNaoReceberMensagensGuardaAData() {
        servico.alterar(CARLA, new MeusDadosRequisicao("Carla Mendes", "92988887777", false));

        assertFalse(cliente.isAceitaMensagens());
        assertEquals(AGORA, cliente.getConsentimentoEm());
    }

    @Test
    void semMudarAOpcaoADataNaoMuda() {
        servico.alterar(CARLA, new MeusDadosRequisicao("Carla Mendes", "92988887777", true));

        assertNull(cliente.getConsentimentoEm());
        verify(clientes, never()).save(any());
    }
}
