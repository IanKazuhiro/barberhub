package br.com.barberhub.catalogo;

import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ADMIN;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.CLIENTE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;
import br.com.barberhub.compartilhado.seguranca.ContasDeTeste;

@WebMvcTest(CatalogoClienteController.class)
@Import(ConfiguracaoSeguranca.class)
class CatalogoClienteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CatalogoServico catalogo;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    @BeforeEach
    void registrarContas() {
        ContasDeTeste.registrar(decodificador, usuarios);
    }

    @Test
    void clienteVeOCatalogo() throws Exception {
        when(catalogo.listar()).thenReturn(List.of(new ItemCatalogoResposta(8L, "Cabelo e barba", 45,
                new BigDecimal("70.00"), true, List.of("Corte", "Barba"))));

        mvc.perform(get("/api/cliente/catalogo").header("Authorization", CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Cabelo e barba"))
                .andExpect(jsonPath("$[0].duracaoMinutos").value(45))
                .andExpect(jsonPath("$[0].preco").value(70.0))
                .andExpect(jsonPath("$[0].itens.length()").value(2));
    }

    @Test
    void outrosPerfisRecebem403() throws Exception {
        mvc.perform(get("/api/cliente/catalogo").header("Authorization", BARBEIRO))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/cliente/catalogo").header("Authorization", ADMIN))
                .andExpect(status().isForbidden());
    }

    @Test
    void semTokenRecebe401() throws Exception {
        mvc.perform(get("/api/cliente/catalogo"))
                .andExpect(status().isUnauthorized());
    }
}
