package br.com.barberhub.compartilhado.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.TokenServico;
import br.com.barberhub.acesso.Usuario;

class TokenJwtTest {

    private final ConfiguracaoJwt configuracao =
            new ConfiguracaoJwt("chave-de-teste-com-pelo-menos-32-caracteres");

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setPerfil(Perfil.BARBEIRO);
        return u;
    }

    @Test
    void tokenGeradoPodeSerLidoEGuardaIdEPerfil() {
        TokenServico servico = new TokenServico(configuracao.codificadorDeToken(), 60);

        Jwt jwt = configuracao.decodificadorDeToken().decode(servico.gerar(usuario()));

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("perfil")).isEqualTo("BARBEIRO");
    }

    @Test
    void tokenExpiradoEhRecusado() {
        Instant agora = Instant.now();
        JwtClaimsSet dados = JwtClaimsSet.builder()
                .subject("7")
                .claim("perfil", "BARBEIRO")
                .issuedAt(agora.minusSeconds(600))
                .expiresAt(agora.minusSeconds(300))
                .build();
        String token = configuracao.codificadorDeToken()
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), dados))
                .getTokenValue();

        assertThatThrownBy(() -> configuracao.decodificadorDeToken().decode(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenAssinadoComOutraChaveEhRecusado() {
        ConfiguracaoJwt outra = new ConfiguracaoJwt("outra-chave-de-teste-com-32-caracteres!!");
        String token = new TokenServico(outra.codificadorDeToken(), 60).gerar(usuario());

        assertThatThrownBy(() -> configuracao.decodificadorDeToken().decode(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void segredoCurtoImpedeAInicializacao() {
        assertThatThrownBy(() -> new ConfiguracaoJwt("curta")).isInstanceOf(IllegalStateException.class);
    }
}