package br.com.barberhub.compartilhado.seguranca;

import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;

/** Contas fictícias para os testes de rota: o token diz quem é, e o repositório diz o perfil. */
public final class ContasDeTeste {

    public static final String ADMIN = "Bearer token-admin";
    public static final String BARBEIRO = "Bearer token-barbeiro";
    public static final String CLIENTE = "Bearer token-cliente";

    public static final long ID_ADMIN = 1L;
    public static final long ID_CLIENTE = 2L;
    public static final long ID_BARBEIRO = 3L;

    private ContasDeTeste() {
    }

    public static void registrar(JwtDecoder decodificador, UsuarioRepositorio usuarios) {
        registrar(decodificador, usuarios, ADMIN, ID_ADMIN, Perfil.ADMINISTRADOR);
        registrar(decodificador, usuarios, CLIENTE, ID_CLIENTE, Perfil.CLIENTE);
        registrar(decodificador, usuarios, BARBEIRO, ID_BARBEIRO, Perfil.BARBEIRO);
    }

    private static void registrar(JwtDecoder decodificador, UsuarioRepositorio usuarios, String cabecalho,
            long id, Perfil perfil) {
        String token = cabecalho.substring("Bearer ".length());
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
}
