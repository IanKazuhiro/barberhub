package br.com.barberhub.barbeiro;

import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ADMIN;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.BARBEIRO;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.CLIENTE;
import static br.com.barberhub.compartilhado.seguranca.ContasDeTeste.ID_ADMIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;
import br.com.barberhub.compartilhado.seguranca.ContasDeTeste;

@WebMvcTest(BarbeiroAdminController.class)
@Import(ConfiguracaoSeguranca.class)
class BarbeiroAdminControllerTest {

    private static final String BASE = "/api/admin/barbeiros";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BarbeiroServico barbeiros;

    @MockitoBean
    private EquipeServico equipe;

    @MockitoBean
    private EscalaServico escalas;

    @MockitoBean
    private FolgaServico folgas;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    @BeforeEach
    void registrarContas() {
        ContasDeTeste.registrar(decodificador, usuarios);
    }

    private static BarbeiroResposta rafael() {
        return new BarbeiroResposta(5L, "Rafael Souza", "rafael@teste.com", "92988887777",
                new BigDecimal("40.00"), true, true);
    }

    @Test
    void listaABarbeiros() throws Exception {
        when(equipe.listar()).thenReturn(List.of(rafael()));

        mvc.perform(get(BASE).header("Authorization", ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Rafael Souza"))
                .andExpect(jsonPath("$[0].percentualComissao").value(40.0));
    }

    @Test
    void alteraUsandoOAdministradorDoToken() throws Exception {
        when(equipe.alterar(eq(ID_ADMIN), eq(5L), any())).thenReturn(rafael());

        mvc.perform(put(BASE + "/5").header("Authorization", ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Rafael Souza\",\"telefone\":\"92988887777\",\"percentualComissao\":40}"))
                .andExpect(status().isOk());

        verify(equipe).alterar(eq(ID_ADMIN), eq(5L), any());
    }

    @Test
    void comissaoAcimaDe100Retorna400() throws Exception {
        mvc.perform(put(BASE + "/5").header("Authorization", ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Rafael\",\"telefone\":\"92988887777\",\"percentualComissao\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("percentualComissao"));
    }

    @Test
    void escalaVaiEVoltaComHoraEMinuto() throws Exception {
        when(escalas.substituir(eq(ID_ADMIN), eq(5L), any()))
                .thenReturn(List.of(new IntervaloEscala(1, LocalTime.of(9, 0), LocalTime.of(12, 0))));

        mvc.perform(put(BASE + "/5/escala").header("Authorization", ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intervalos\":[{\"diaSemana\":1,\"inicio\":\"09:00\",\"fim\":\"12:00\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inicio").value("09:00"))
                .andExpect(jsonPath("$[0].fim").value("12:00"));
    }

    @Test
    void diaDaSemanaInvalidoRetorna400() throws Exception {
        mvc.perform(put(BASE + "/5/escala").header("Authorization", ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intervalos\":[{\"diaSemana\":7,\"inicio\":\"09:00\",\"fim\":\"12:00\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("intervalos[0].diaSemana"));
    }

    @Test
    void criaFolgaERetorna201() throws Exception {
        when(folgas.criar(eq(ID_ADMIN), eq(5L), any()))
                .thenReturn(new FolgaResposta(7L, LocalDate.of(2026, 12, 24), "Natal"));

        mvc.perform(post(BASE + "/5/folgas").header("Authorization", ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"2026-12-24\",\"motivo\":\"Natal\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value("2026-12-24"));
    }

    @Test
    void folgaRepetidaRetorna422() throws Exception {
        when(folgas.criar(any(), any(), any())).thenThrow(new ExcecaoNegocio(FolgaServico.REPETIDA));

        mvc.perform(post(BASE + "/5/folgas").header("Authorization", ADMIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"2026-12-24\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void removeFolgaERetorna204() throws Exception {
        mvc.perform(delete(BASE + "/5/folgas/7").header("Authorization", ADMIN))
                .andExpect(status().isNoContent());

        verify(folgas).remover(ID_ADMIN, 5L, 7L);
    }

    @Test
    void outrosPerfisRecebem403ESemToken401() throws Exception {
        mvc.perform(get(BASE).header("Authorization", BARBEIRO)).andExpect(status().isForbidden());
        mvc.perform(get(BASE + "/5/escala").header("Authorization", CLIENTE)).andExpect(status().isForbidden());
        mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }
}
