package br.com.barberhub.agendamento;

import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ADMIN;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.CLIENTE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;
import br.com.barberhub.compartilhado.seguranca.ContasDeTeste;

@WebMvcTest(HistoricoController.class)
@Import(ConfiguracaoSeguranca.class)
class HistoricoControllerTest {

    private static final String HISTORICO = "/api/clientes/2/historico";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private HistoricoServico historico;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    @BeforeEach
    void registrarContas() {
        ContasDeTeste.registrar(decodificador, usuarios);
    }

    @Test
    void administradorEBarbeiroVeemOHistorico() throws Exception {
        when(historico.doCliente(2L)).thenReturn(List.of(new AtendimentoResposta(9L,
                LocalDateTime.of(2026, 11, 10, 10, 0), "Corte", "Rafael Souza", new BigDecimal("45.00"))));

        for (String quem : List.of(ADMIN, BARBEIRO)) {
            mvc.perform(get(HISTORICO).header("Authorization", quem))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].inicio").value("2026-11-10T10:00"))
                    .andExpect(jsonPath("$[0].servico").value("Corte"))
                    .andExpect(jsonPath("$[0].barbeiro").value("Rafael Souza"))
                    .andExpect(jsonPath("$[0].valor").value(45.0));
        }
    }

    @Test
    void clienteInexistenteRetorna404() throws Exception {
        when(historico.doCliente(99L))
                .thenThrow(new RecursoNaoEncontradoException(HistoricoServico.CLIENTE_NAO_ENCONTRADO));

        mvc.perform(get("/api/clientes/99/historico").header("Authorization", ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteRecebe403ESemToken401() throws Exception {
        mvc.perform(get(HISTORICO).header("Authorization", CLIENTE)).andExpect(status().isForbidden());
        mvc.perform(get(HISTORICO)).andExpect(status().isUnauthorized());
    }
}
