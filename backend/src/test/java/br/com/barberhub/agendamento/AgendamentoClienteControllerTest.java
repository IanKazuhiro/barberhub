package br.com.barberhub.agendamento;

import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ADMIN;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.CLIENTE;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ID_CLIENTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import br.com.barberhub.compartilhado.erro.ConflitoException;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;
import br.com.barberhub.compartilhado.seguranca.ContasDeTeste;

@WebMvcTest(AgendamentoClienteController.class)
@Import(ConfiguracaoSeguranca.class)
class AgendamentoClienteControllerTest {

    private static final String DISPONIBILIDADE = "/api/cliente/disponibilidade";
    private static final String AGENDAMENTOS = "/api/cliente/agendamentos";
    private static final String PEDIDO = "{\"servicoId\":1,\"barbeiroId\":5,\"inicio\":\"2026-12-02T10:00\"}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DisponibilidadeServico disponibilidade;

    @MockitoBean
    private AgendamentoServico agendamentos;

    @MockitoBean
    private MeusAgendamentosServico meusAgendamentos;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    @BeforeEach
    void registrarContas() {
        ContasDeTeste.registrar(decodificador, usuarios);
    }

    private static AgendamentoResposta resposta() {
        LocalDateTime inicio = LocalDateTime.of(2026, 12, 2, 10, 0);
        return new AgendamentoResposta(9L, inicio, inicio.plusMinutes(30), EstadoAgendamento.AGENDADO, 1L, "Corte",
                5L, "Rafael Souza", new BigDecimal("45.00"));
    }

    @Test
    void devolveOsHorariosComHoraEMinuto() throws Exception {
        when(disponibilidade.horariosLivres(5L, 1L, LocalDate.of(2026, 12, 2)))
                .thenReturn(List.of(LocalTime.of(9, 0), LocalTime.of(9, 15)));

        mvc.perform(get(DISPONIBILIDADE + "?barbeiroId=5&servicoId=1&data=2026-12-02")
                        .header("Authorization", CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("09:00"))
                .andExpect(jsonPath("$[1]").value("09:15"));
    }

    @Test
    void dataInvalidaOuAusenteRetorna400() throws Exception {
        mvc.perform(get(DISPONIBILIDADE + "?barbeiroId=5&servicoId=1&data=amanha").header("Authorization", CLIENTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("data"));
        mvc.perform(get(DISPONIBILIDADE + "?barbeiroId=5&servicoId=1").header("Authorization", CLIENTE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agendaParaOClienteDoTokenERetorna201() throws Exception {
        when(agendamentos.agendar(eq(ID_CLIENTE), any())).thenReturn(resposta());

        mvc.perform(post(AGENDAMENTOS).header("Authorization", CLIENTE)
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("AGENDADO"))
                .andExpect(jsonPath("$.inicio").value("2026-12-02T10:00"));

        verify(agendamentos).agendar(eq(ID_CLIENTE), any());
    }

    @Test
    void semEscolhasRetorna400() throws Exception {
        mvc.perform(post(AGENDAMENTOS).header("Authorization", CLIENTE)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(3));
    }

    @Test
    void horarioOcupadoRetorna409ComAMensagem() throws Exception {
        when(agendamentos.agendar(any(), any()))
                .thenThrow(new ConflitoException(DisponibilidadeServico.HORARIO_OCUPADO));

        mvc.perform(post(AGENDAMENTOS).header("Authorization", CLIENTE)
                        .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"))
                .andExpect(jsonPath("$.mensagem").value(DisponibilidadeServico.HORARIO_OCUPADO));
    }

    @Test
    void listaSomenteOsAgendamentosDoToken() throws Exception {
        when(meusAgendamentos.listar(ID_CLIENTE))
                .thenReturn(new MeusAgendamentosResposta(List.of(resposta()), List.of()));

        mvc.perform(get(AGENDAMENTOS).header("Authorization", CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proximos[0].servico").value("Corte"))
                .andExpect(jsonPath("$.proximos[0].barbeiro").value("Rafael Souza"))
                .andExpect(jsonPath("$.anteriores.length()").value(0));
        mvc.perform(get(AGENDAMENTOS).header("Authorization", BARBEIRO)).andExpect(status().isForbidden());

        verify(meusAgendamentos).listar(ID_CLIENTE);
    }

    @Test
    void soOClienteAgenda() throws Exception {
        for (String quem : List.of(ADMIN, BARBEIRO)) {
            mvc.perform(post(AGENDAMENTOS).header("Authorization", quem)
                            .contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(post(AGENDAMENTOS).contentType(MediaType.APPLICATION_JSON).content(PEDIDO))
                .andExpect(status().isUnauthorized());
    }
}
