package br.com.barberhub.catalogo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;

@WebMvcTest(ServicoAdminController.class)
@Import(ConfiguracaoSeguranca.class)
class ServicoAdminControllerTest {

    private static final String ADMIN = "Bearer token-admin";
    private static final String CLIENTE = "Bearer token-cliente";
    private static final String CORPO_VALIDO = "{\"nome\":\"Corte masculino\",\"duracaoMinutos\":30,\"preco\":40.00}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicoServico servicos;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    /** Administrador (id 1) e cliente (id 2), ambos ativos e com token válido. */
    @BeforeEach
    void registrarContas() {
        registrarConta("token-admin", 1L, Perfil.ADMINISTRADOR);
        registrarConta("token-cliente", 2L, Perfil.CLIENTE);
    }

    private void registrarConta(String token, long id, Perfil perfil) {
        Instant agora = Instant.now();
        Jwt jwt = Jwt.withTokenValue(token)
                .header("alg", "HS256")
                .subject(String.valueOf(id))
                .claim("perfil", perfil.name())
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(600))
                .build();
        when(decodificador.decode(token)).thenReturn(jwt);

        Usuario conta = new Usuario();
        conta.setId(id);
        conta.setPerfil(perfil);
        conta.setAtivo(true);
        when(usuarios.findById(id)).thenReturn(Optional.of(conta));
    }

    private static ServicoResposta resposta(Long id, boolean ativo) {
        Instant agora = Instant.now();
        return new ServicoResposta(id, "Corte masculino", 30, new BigDecimal("40.00"),
                false, ativo, agora, agora);
    }

    @Test
    void criarChamaOServicoComOAdministradorDoTokenERetorna201() throws Exception {
        when(servicos.criar(eq(1L), any())).thenReturn(resposta(7L, true));

        mvc.perform(post("/api/admin/servicos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Corte masculino"));

        verify(servicos).criar(eq(1L), any());
    }

    @Test
    void criarSemNomeRetorna400ComCampo() throws Exception {
        mvc.perform(post("/api/admin/servicos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"duracaoMinutos\":30,\"preco\":40}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos[0].campo").value("nome"));
    }

    @Test
    void criarComDuracaoZeroRetorna400() throws Exception {
        mvc.perform(post("/api/admin/servicos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Corte\",\"duracaoMinutos\":0,\"preco\":40}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("duracaoMinutos"));
    }

    @Test
    void criarComPrecoNegativoRetorna400() throws Exception {
        mvc.perform(post("/api/admin/servicos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Corte\",\"duracaoMinutos\":30,\"preco\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("preco"));
    }

    @Test
    void criarComPrecoDeTresCasasRetorna400() throws Exception {
        mvc.perform(post("/api/admin/servicos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Corte\",\"duracaoMinutos\":30,\"preco\":10.123}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("preco"));
    }

    @Test
    void nomeRepetidoRetorna422() throws Exception {
        when(servicos.criar(any(), any())).thenThrow(new ExcecaoNegocio(ServicoServico.NOME_REPETIDO));

        mvc.perform(post("/api/admin/servicos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"));
    }

    @Test
    void alterarChamaOServicoComIdDaRotaERetorna200() throws Exception {
        when(servicos.alterar(eq(1L), eq(3L), any())).thenReturn(resposta(3L, true));

        mvc.perform(put("/api/admin/servicos/3").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));

        verify(servicos).alterar(eq(1L), eq(3L), any());
    }

    @Test
    void alterarServicoInexistenteRetorna404() throws Exception {
        when(servicos.alterar(any(), any(), any()))
                .thenThrow(new RecursoNaoEncontradoException(ServicoServico.NAO_ENCONTRADO));

        mvc.perform(put("/api/admin/servicos/99").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
    }

    @Test
    void inativarChamaOServicoComAtivoFalso() throws Exception {
        when(servicos.alterarSituacao(1L, 3L, false)).thenReturn(resposta(3L, false));

        mvc.perform(patch("/api/admin/servicos/3/inativar").header("Authorization", ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        verify(servicos).alterarSituacao(1L, 3L, false);
    }

    @Test
    void ativarChamaOServicoComAtivoVerdadeiro() throws Exception {
        when(servicos.alterarSituacao(1L, 3L, true)).thenReturn(resposta(3L, true));

        mvc.perform(patch("/api/admin/servicos/3/ativar").header("Authorization", ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));

        verify(servicos).alterarSituacao(1L, 3L, true);
    }

    @Test
    void listarRepassaOFiltroDeSituacao() throws Exception {
        when(servicos.listar(true)).thenReturn(List.of(resposta(1L, true)));

        mvc.perform(get("/api/admin/servicos?ativo=true").header("Authorization", ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Corte masculino"));

        verify(servicos).listar(true);
    }

    @Test
    void clienteRecebe403() throws Exception {
        mvc.perform(get("/api/admin/servicos").header("Authorization", CLIENTE))
                .andExpect(status().isForbidden());
    }

    @Test
    void semTokenRecebe401() throws Exception {
        mvc.perform(get("/api/admin/servicos"))
                .andExpect(status().isUnauthorized());
    }
}