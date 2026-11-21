package br.com.barberhub.acesso;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.seguranca.ConfiguracaoSeguranca;

@WebMvcTest(AcessoController.class)
@Import(ConfiguracaoSeguranca.class)
class AcessoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CadastroServico cadastro;

    @MockitoBean
    private LoginServico login;

    @MockitoBean
    private JwtDecoder decodificador;

    @MockitoBean
    private UsuarioRepositorio usuarios;

    private String corpo(String email, String senha) {
        return """
            {"nome":"Ana Souza","email":"%s","telefone":"92999998888","senha":"%s"}
            """.formatted(email, senha);
    }

    @Test
    void cadastroValidoRetorna201SemDevolverSenha() throws Exception {
        when(cadastro.cadastrar(any())).thenReturn(
                new UsuarioResposta(1L, "Ana Souza", "ana@teste.com", "92999998888", Perfil.CLIENTE));

        mvc.perform(post("/api/acesso/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ana@teste.com", "senha1234")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void senhaCurtaRetorna400() throws Exception {
        mvc.perform(post("/api/acesso/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ana@teste.com", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos[0].campo").value("senha"));
    }

    @Test
    void emailInvalidoRetorna400() throws Exception {
        mvc.perform(post("/api/acesso/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ana", "senha1234")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("email"));
    }

    @Test
    void emailRepetidoRetorna422ComMensagem() throws Exception {
        when(cadastro.cadastrar(any())).thenThrow(new ExcecaoNegocio(CadastroServico.EMAIL_REPETIDO));

        mvc.perform(post("/api/acesso/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ana@teste.com", "senha1234")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("REGRA_DE_NEGOCIO"))
                .andExpect(jsonPath("$.mensagem").value(CadastroServico.EMAIL_REPETIDO));
    }
}