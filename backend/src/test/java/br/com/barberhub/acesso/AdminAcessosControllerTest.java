package br.com.barberhub.acesso;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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

import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;

@WebMvcTest(AdminAcessosController.class)
@Import(ConfiguracaoSeguranca.class)
class AdminAcessosControllerTest {

    private static final String ADMIN = "Bearer token-admin";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GestaoAcessosServico acessos;

    @MockitoBean
    private TrocaPerfilServico perfis;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    /** Administrador com id 1, ativo, logado com um token válido. */
    @BeforeEach
    void logarComoAdministrador() {
        Instant agora = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token-admin")
                .header("alg", "HS256")
                .subject("1")
                .claim("perfil", "ADMINISTRADOR")
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(600))
                .build();
        when(decodificador.decode("token-admin")).thenReturn(jwt);

        Usuario conta = new Usuario();
        conta.setId(1L);
        conta.setPerfil(Perfil.ADMINISTRADOR);
        conta.setAtivo(true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(conta));
    }

    @Test
    void filtroDePerfilInvalidoRetorna400EmVezDe500() throws Exception {
        mvc.perform(get("/api/admin/usuarios?perfil=XYZ").header("Authorization", ADMIN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos[0].campo").value("perfil"));
    }

    @Test
    void idQueNaoEhNumeroRetorna400() throws Exception {
        mvc.perform(patch("/api/admin/usuarios/abc/inativar").header("Authorization", ADMIN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos[0].campo").value("id"));
    }

    @Test
    void trocarPerfilChamaOServicoComOAdministradorDoToken() throws Exception {
        when(perfis.trocar(1L, 7L, Perfil.BARBEIRO)).thenReturn(new UsuarioAdminResposta(
                7L, "Rafael Souza", "rafael@teste.com", "92999990000", Perfil.BARBEIRO, true, Instant.now()));

        mvc.perform(patch("/api/admin/usuarios/7/perfil").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfil\":\"BARBEIRO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("BARBEIRO"));

        verify(perfis).trocar(1L, 7L, Perfil.BARBEIRO);
    }

    @Test
    void trocarPerfilSemPerfilRetorna400ComCampo() throws Exception {
        mvc.perform(patch("/api/admin/usuarios/7/perfil").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("perfil"));
    }

    @Test
    void trocarPerfilParaValorDesconhecidoRetorna400() throws Exception {
        mvc.perform(patch("/api/admin/usuarios/7/perfil").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfil\":\"DONO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
    }

    @Test
    void regraDeNegocioDaTrocaRetorna422() throws Exception {
        when(perfis.trocar(any(), any(), any())).thenThrow(new ExcecaoNegocio(TrocaPerfilServico.NAO_TROCA_O_PROPRIO));

        mvc.perform(patch("/api/admin/usuarios/1/perfil").header("Authorization", ADMIN)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfil\":\"CLIENTE\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"));
    }
}
