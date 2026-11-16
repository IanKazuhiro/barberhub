package br.com.barberhub.acesso;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.barberhub.compartilhado.erro.AcessoNegadoException;
import br.com.barberhub.compartilhado.erro.NaoAutenticadoException;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;

@WebMvcTest(AcessoController.class)
@Import({ ConfiguracaoSeguranca.class, AcessoSegurancaTest.RotasDeTeste.class })
class AcessoSegurancaTest {

    /** Rotas só de teste, para provar a regra de cada perfil antes de existirem as telas reais. */
    @RestController
    public static class RotasDeTeste {
        @GetMapping("/api/admin/teste")
        public String admin() {
            return "ok";
        }

        @GetMapping("/api/barbeiro/teste")
        public String barbeiro() {
            return "ok";
        }

        @GetMapping("/api/cliente/teste")
        public String cliente() {
            return "ok";
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CadastroServico cadastro;

    @MockitoBean
    private LoginServico login;

    @MockitoBean
    private JwtDecoder decodificador;

    private String tokenDe(String perfil) {
        Instant agora = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token-" + perfil)
                .header("alg", "HS256")
                .subject("1")
                .claim("perfil", perfil)
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(600))
                .build();
        when(decodificador.decode(jwt.getTokenValue())).thenReturn(jwt);
        return "Bearer " + jwt.getTokenValue();
    }

    private static final String LOGIN = """
            {"email":"ana@teste.com","senha":"senha1234"}
            """;

    @Test
    void loginEhPublicoERetornaTokenEUsuario() throws Exception {
        var usuario = new UsuarioResposta(1L, "Ana Souza", "ana@teste.com", "92999998888", Perfil.CLIENTE);
        when(login.entrar(any())).thenReturn(new LoginResposta("tk", "Bearer", 3600, usuario));

        mvc.perform(post("/api/acesso/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tk"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.senhaHash").doesNotExist());
    }

    @Test
    void loginComCamposVaziosRetorna400() throws Exception {
        mvc.perform(post("/api/acesso/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));
    }

    @Test
    void loginComCredenciaisErradasRetorna401() throws Exception {
        when(login.entrar(any())).thenThrow(new NaoAutenticadoException("E-mail ou senha incorretos."));

        mvc.perform(post("/api/acesso/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"))
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos."));
    }

    @Test
    void loginDeContaInativaRetorna403() throws Exception {
        when(login.entrar(any())).thenThrow(new AcessoNegadoException("Esta conta está inativa."));

        mvc.perform(post("/api/acesso/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void semTokenRotaProtegidaRetorna401ComCorpoPadrao() throws Exception {
        mvc.perform(get("/api/acesso/eu"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        when(decodificador.decode("lixo")).thenThrow(new BadJwtException("inválido"));

        mvc.perform(get("/api/acesso/eu").header("Authorization", "Bearer lixo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
    }

    @Test
    void euRetornaOUsuarioDoToken() throws Exception {
        when(login.usuarioAtual(1L)).thenReturn(
                new UsuarioResposta(1L, "Ana Souza", "ana@teste.com", "92999998888", Perfil.CLIENTE));

        mvc.perform(get("/api/acesso/eu").header("Authorization", tokenDe("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@teste.com"))
                .andExpect(jsonPath("$.perfil").value("CLIENTE"));
    }

    @Test
    void clienteNaoAcessaRotaDeAdministrador() throws Exception {
        mvc.perform(get("/api/admin/teste").header("Authorization", tokenDe("CLIENTE")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void barbeiroNaoAcessaRotaDeAdministrador() throws Exception {
        mvc.perform(get("/api/admin/teste").header("Authorization", tokenDe("BARBEIRO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorAcessaRotaDeAdministrador() throws Exception {
        mvc.perform(get("/api/admin/teste").header("Authorization", tokenDe("ADMINISTRADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void barbeiroAcessaSuaRotaEClienteAcessaASua() throws Exception {
        mvc.perform(get("/api/barbeiro/teste").header("Authorization", tokenDe("BARBEIRO")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/cliente/teste").header("Authorization", tokenDe("CLIENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void perfilNaoAcessaRotaDeOutroPerfil() throws Exception {
        mvc.perform(get("/api/barbeiro/teste").header("Authorization", tokenDe("CLIENTE")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/cliente/teste").header("Authorization", tokenDe("ADMINISTRADOR")))
                .andExpect(status().isForbidden());
    }
}