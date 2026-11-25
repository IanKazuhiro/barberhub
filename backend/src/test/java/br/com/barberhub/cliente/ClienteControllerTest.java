package br.com.barberhub.cliente;

import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ADMIN;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.CLIENTE;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ID_BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ID_CLIENTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;
import br.com.barberhub.compartilhado.seguranca.ContasDeTeste;

@WebMvcTest({ClienteController.class, ClienteAdminController.class, MeusDadosController.class})
@Import(ConfiguracaoSeguranca.class)
class ClienteControllerTest {

    private static final String DADOS = "{\"nome\":\"Carla Mendes\",\"telefone\":\"92988887777\"}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClienteServico clientes;

    @MockitoBean
    private MeusDadosServico meusDados;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    @BeforeEach
    void registrarContas() {
        ContasDeTeste.registrar(decodificador, usuarios);
    }

    @Test
    void administradorEBarbeiroBuscamClientes() throws Exception {
        when(clientes.buscar("carla")).thenReturn(
                List.of(new ClienteResumoResposta(2L, "Carla Mendes", "carla@teste.com", "92988887777", true)));

        for (String quem : List.of(ADMIN, BARBEIRO)) {
            mvc.perform(get("/api/clientes?busca=carla").header("Authorization", quem))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nome").value("Carla Mendes"));
        }
    }

    @Test
    void clienteNaoVeOsOutrosClientes() throws Exception {
        mvc.perform(get("/api/clientes").header("Authorization", CLIENTE)).andExpect(status().isForbidden());
        mvc.perform(get("/api/clientes/5").header("Authorization", CLIENTE)).andExpect(status().isForbidden());
        mvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized());
    }

    @Test
    void barbeiroRegistraPreferenciasMasNaoAlteraDados() throws Exception {
        mvc.perform(put("/api/clientes/2/preferencias").header("Authorization", BARBEIRO)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"observacoes\":\"Prefere tesoura\"}"))
                .andExpect(status().isOk());
        verify(clientes).alterarPreferencias(eq(ID_BARBEIRO), eq(2L), any());

        mvc.perform(put("/api/admin/clientes/2").header("Authorization", BARBEIRO)
                        .contentType(MediaType.APPLICATION_JSON).content(DADOS))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorAlteraDados() throws Exception {
        mvc.perform(put("/api/admin/clientes/2").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(DADOS))
                .andExpect(status().isOk());
    }

    @Test
    void telefoneInvalidoRetorna400() throws Exception {
        mvc.perform(put("/api/admin/clientes/2").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Carla\",\"telefone\":\"12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("telefone"));
    }

    @Test
    void clienteAlteraSomenteOProprioCadastro() throws Exception {
        when(meusDados.alterar(eq(ID_CLIENTE), any()))
                .thenReturn(new MeusDadosResposta("Carla Mendes", "carla@teste.com", "92988887777", false));

        mvc.perform(put("/api/cliente/dados").header("Authorization", CLIENTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"nome\":\"Carla Mendes\",\"telefone\":\"92988887777\",\"aceitaMensagens\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aceitaMensagens").value(false));

        verify(meusDados).alterar(eq(ID_CLIENTE), any());
    }

    @Test
    void semAOpcaoDeMensagensRetorna400() throws Exception {
        mvc.perform(put("/api/cliente/dados").header("Authorization", CLIENTE)
                        .contentType(MediaType.APPLICATION_JSON).content(DADOS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("aceitaMensagens"));
    }

    @Test
    void meusDadosEhSoDoCliente() throws Exception {
        mvc.perform(get("/api/cliente/dados").header("Authorization", BARBEIRO)).andExpect(status().isForbidden());
    }
}
