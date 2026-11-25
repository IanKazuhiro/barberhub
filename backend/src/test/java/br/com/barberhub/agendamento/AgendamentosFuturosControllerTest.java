package br.com.barberhub.agendamento;

import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ADMIN;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.CLIENTE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

@WebMvcTest(AgendamentosFuturosController.class)
@Import(ConfiguracaoSeguranca.class)
class AgendamentosFuturosControllerTest {

    private static final String ROTA = "/api/admin/barbeiros/5/agendamentos-futuros";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AgendamentosFuturosServico futuros;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    @BeforeEach
    void registrarContas() {
        ContasDeTeste.registrar(decodificador, usuarios);
    }

    @Test
    void administradorVeALista() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(2026, 12, 2, 10, 0);
        when(futuros.doBarbeiro(5L)).thenReturn(List.of(new AgendamentoFuturoResposta(9L, inicio,
                inicio.plusMinutes(30), "Carla Mendes", "92988887777", "Corte")));

        mvc.perform(get(ROTA).header("Authorization", ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inicio").value("2026-12-02T10:00"))
                .andExpect(jsonPath("$[0].cliente").value("Carla Mendes"));
    }

    @Test
    void outrosPerfisRecebem403ESemToken401() throws Exception {
        mvc.perform(get(ROTA).header("Authorization", BARBEIRO)).andExpect(status().isForbidden());
        mvc.perform(get(ROTA).header("Authorization", CLIENTE)).andExpect(status().isForbidden());
        mvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
    }
}
