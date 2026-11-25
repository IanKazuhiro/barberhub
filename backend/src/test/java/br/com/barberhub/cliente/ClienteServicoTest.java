package br.com.barberhub.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.barberhub.acesso.Cliente;
import br.com.barberhub.acesso.ClienteRepositorio;
import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class ClienteServicoTest {

    private static final long ADMIN = 1L;
    private static final long CARLA = 2L;
    private static final long RAFAEL = 3L;

    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final ClienteRepositorio clientes = mock(ClienteRepositorio.class);
    private final ClienteConsultaRepositorio consulta = mock(ClienteConsultaRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final ClienteServico servico = new ClienteServico(usuarios, clientes, consulta, auditoria);

    private final Cliente carla = new Cliente(CARLA);
    private final Usuario usuarioCarla = usuario(CARLA, "Carla Mendes", Perfil.CLIENTE, true);

    private static Usuario usuario(long id, String nome, Perfil perfil, boolean ativo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setTelefone("92988887777");
        u.setPerfil(perfil);
        u.setAtivo(ativo);
        return u;
    }

    @BeforeEach
    void existeACarla() {
        when(clientes.findById(CARLA)).thenReturn(Optional.of(carla));
        when(usuarios.findById(CARLA)).thenReturn(Optional.of(usuarioCarla));
    }

    private void existeBarbeiro(boolean ativo) {
        when(usuarios.findById(RAFAEL)).thenReturn(Optional.of(usuario(RAFAEL, "Rafael Souza", Perfil.BARBEIRO, ativo)));
    }

    @Test
    void buscaPorParteDoNomeSemAcentoNemMaiuscula() {
        when(consulta.buscar(eq("%joao%"), eq(""), any())).thenReturn(List.of(usuarioCarla));

        assertEquals(1, servico.buscar("  JOÃO ").size());
    }

    @Test
    void buscaPorParteDoTelefoneIgnoraAPontuacao() {
        assertEquals("%9298888%", ClienteServico.padraoDoTelefone("(92) 9-8888"));
        assertEquals("%9298888%", ClienteServico.padraoDoTelefone("92 98888"));
    }

    @Test
    void textoComLetrasNaoViraBuscaPorTelefone() {
        assertEquals("", ClienteServico.padraoDoTelefone("Rafael 2"));
        assertEquals("", ClienteServico.padraoDoTelefone(""));
    }

    @Test
    void curingasDigitadosSaoTextoComum() {
        assertEquals("%100!%!_a!!%", ClienteServico.padraoDoNome("100%_a!"));
    }

    @Test
    void buscaVaziaListaTodos() {
        servico.buscar(null);

        verify(consulta).buscar(eq("%%"), eq(""), any());
    }

    @Test
    void administradorAlteraNomeETelefoneERegistraAuditoria() {
        ClienteDetalheResposta resposta = servico.alterarDados(ADMIN, CARLA,
                new DadosClienteRequisicao(" Carla M. Mendes ", "92977776666"));

        assertEquals("Carla M. Mendes", resposta.nome());
        assertEquals("92977776666", usuarioCarla.getTelefone());
        verify(auditoria).registrar(ADMIN, "ALTERAR_CLIENTE", "cliente", CARLA);
    }

    @Test
    void registraPreferenciasComBarbeiroFavorito() {
        existeBarbeiro(true);

        ClienteDetalheResposta resposta = servico.alterarPreferencias(RAFAEL, CARLA,
                new PreferenciasRequisicao(RAFAEL, "Degradê baixo", " Prefere tesoura no topo "));

        assertEquals("Rafael Souza", resposta.barbeiroFavorito());
        assertEquals("Prefere tesoura no topo", carla.getObservacoes());
        verify(auditoria).registrar(RAFAEL, "ALTERAR_PREFERENCIAS_CLIENTE", "cliente", CARLA);
    }

    @Test
    void textosEmBrancoLimpamAsPreferencias() {
        carla.setTipoCorte("Social");
        carla.setObservacoes("Algo");

        servico.alterarPreferencias(ADMIN, CARLA, new PreferenciasRequisicao(null, " ", ""));

        assertNull(carla.getTipoCorte());
        assertNull(carla.getObservacoes());
    }

    @Test
    void recusaFavoritoInativoOuQueNaoEhBarbeiro() {
        existeBarbeiro(false);

        assertThrows(ExcecaoNegocio.class, () -> servico.alterarPreferencias(ADMIN, CARLA,
                new PreferenciasRequisicao(RAFAEL, null, null)));
        assertThrows(ExcecaoNegocio.class, () -> servico.alterarPreferencias(ADMIN, CARLA,
                new PreferenciasRequisicao(CARLA, null, null)));
        verify(clientes, never()).save(any());
    }

    @Test
    void favoritoQueFoiInativadoDepoisPodeContinuar() {
        existeBarbeiro(false);
        carla.setBarbeiroFavoritoId(RAFAEL);

        servico.alterarPreferencias(ADMIN, CARLA, new PreferenciasRequisicao(RAFAEL, "Social", null));

        assertEquals("Social", carla.getTipoCorte());
    }

    @Test
    void quemNaoEhClienteDaNaoEncontrado() {
        when(clientes.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.detalhar(99L));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.alterarDados(ADMIN, 99L, new DadosClienteRequisicao("Fulano", "92977776666")));
    }
}
