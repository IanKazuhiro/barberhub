package br.com.barberhub.catalogo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@WebMvcTest(ComboAdminController.class)
@Import(ConfiguracaoSeguranca.class)
class ComboAdminControllerTest {

    private static final String ADMIN = "Bearer token-admin";
    private static final String CLIENTE = "Bearer token-cliente";
    private static final String CORPO_VALIDO =
            "{\"nome\":\"Combo corte e barba\",\"preco\":60.00,\"servicoIds\":[1,2]}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ComboServico combos;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

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

    private static ComboResposta resposta(Long id, boolean ativo) {
        Instant agora = Instant.now();
        List<ComboResposta.ItemResposta> itens = List.of(
                new ComboResposta.ItemResposta(1L, "Corte", 30),
                new ComboResposta.ItemResposta(2L, "Barba", 20));
        return new ComboResposta(id, "Combo corte e barba", new BigDecimal("60.00"), 50,
                ativo, agora, agora, itens);
    }

    @Test
    void criarChamaOComboComOAdministradorDoTokenERetorna201() throws Exception {
        when(combos.criar(eq(1L), any())).thenReturn(resposta(7L, true));

        mvc.perform(post("/api/admin/combos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.itens.length()").value(2));

        verify(combos).criar(eq(1L), any());
    }

    @Test
    void criarComMenosDeDoisItensRetorna400() throws Exception {
        mvc.perform(post("/api/admin/combos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Combo\",\"preco\":10,\"servicoIds\":[1]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("servicoIds"));
    }

    @Test
    void itemInvalidoRetorna422() throws Exception {
        when(combos.criar(any(), any())).thenThrow(new ExcecaoNegocio(ComboServico.ITEM_INVALIDO));

        mvc.perform(post("/api/admin/combos").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"));
    }

    @Test
    void alterarChamaOComboComIdDaRotaERetorna200() throws Exception {
        when(combos.alterar(eq(1L), eq(3L), any())).thenReturn(resposta(3L, true));

        mvc.perform(put("/api/admin/combos/3").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));

        verify(combos).alterar(eq(1L), eq(3L), any());
    }

    @Test
    void alterarComboInexistenteRetorna404() throws Exception {
        when(combos.alterar(any(), any(), any()))
                .thenThrow(new RecursoNaoEncontradoException(ComboServico.NAO_ENCONTRADO));

        mvc.perform(put("/api/admin/combos/99").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
    }

    @Test
    void listarRepassaOFiltroDeSituacao() throws Exception {
        when(combos.listar(true)).thenReturn(List.of(resposta(1L, true)));

        mvc.perform(get("/api/admin/combos?ativo=true").header("Authorization", ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Combo corte e barba"));

        verify(combos).listar(true);
    }

    @Test
    void clienteRecebe403() throws Exception {
        mvc.perform(get("/api/admin/combos").header("Authorization", CLIENTE))
                .andExpect(status().isForbidden());
    }

    @Test
    void semTokenRecebe401() throws Exception {
        mvc.perform(get("/api/admin/combos"))
                .andExpect(status().isUnauthorized());
    }
}