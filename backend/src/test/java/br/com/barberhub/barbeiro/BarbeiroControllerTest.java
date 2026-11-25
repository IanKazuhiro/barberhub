package br.com.barberhub.barbeiro;

import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ADMIN;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.CLIENTE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(BarbeiroController.class)
@Import(ConfiguracaoSeguranca.class)
class BarbeiroControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EquipeServico equipe;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    @BeforeEach
    void registrarContas() {
        ContasDeTeste.registrar(decodificador, usuarios);
    }

    @Test
    void qualquerLogadoVeOsAtivosSemContatoNemComissao() throws Exception {
        when(equipe.listarAtivos()).thenReturn(List.of(new BarbeiroAtivoResposta(5L, "Rafael Souza")));

        for (String quem : List.of(ADMIN, BARBEIRO, CLIENTE)) {
            mvc.perform(get("/api/barbeiros").header("Authorization", quem))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nome").value("Rafael Souza"))
                    .andExpect(jsonPath("$[0].email").doesNotExist())
                    .andExpect(jsonPath("$[0].percentualComissao").doesNotExist());
        }
    }

    @Test
    void semTokenRecebe401() throws Exception {
        mvc.perform(get("/api/barbeiros")).andExpect(status().isUnauthorized());
    }
}
