package br.com.barberhub.agendamento;

import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ADMIN;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.CLIENTE;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ID_BARBEIRO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
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

@WebMvcTest(AgendaController.class)
@Import(ConfiguracaoSeguranca.class)
class AgendaControllerTest {

    private static final String AGENDA = "/api/barbeiro/agenda";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AgendaServico agenda;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    @BeforeEach
    void registrarContas() {
        ContasDeTeste.registrar(decodificador, usuarios);
    }

    @Test
    void barbeiroVeAPropriaAgendaMesmoPedindoADeOutro() throws Exception {
        when(agenda.doDia(ID_BARBEIRO, null)).thenReturn(List.of(new ItemAgendaResposta(9L, LocalTime.of(10, 0),
                LocalTime.of(10, 30), EstadoAgendamento.AGENDADO, 2L, "Carla Mendes", "92988887777", "Corte",
                null, null)));

        mvc.perform(get(AGENDA + "?barbeiroId=99").header("Authorization", BARBEIRO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inicio").value("10:00"))
                .andExpect(jsonPath("$[0].cliente").value("Carla Mendes"));

        verify(agenda).doDia(ID_BARBEIRO, null);
    }

    @Test
    void repassaADataPedida() throws Exception {
        mvc.perform(get(AGENDA + "?data=2026-12-10").header("Authorization", BARBEIRO)).andExpect(status().isOk());

        verify(agenda).doDia(ID_BARBEIRO, LocalDate.of(2026, 12, 10));
    }

    @Test
    void dataInvalidaRetorna400() throws Exception {
        mvc.perform(get(AGENDA + "?data=hoje").header("Authorization", BARBEIRO)).andExpect(status().isBadRequest());
    }

    @Test
    void outrosPerfisRecebem403ESemToken401() throws Exception {
        mvc.perform(get(AGENDA).header("Authorization", ADMIN)).andExpect(status().isForbidden());
        mvc.perform(get(AGENDA).header("Authorization", CLIENTE)).andExpect(status().isForbidden());
        mvc.perform(get(AGENDA)).andExpect(status().isUnauthorized());
    }
}
