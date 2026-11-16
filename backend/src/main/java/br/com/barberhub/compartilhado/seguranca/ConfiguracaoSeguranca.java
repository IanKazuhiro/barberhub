package br.com.barberhub.compartilhado.seguranca;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class ConfiguracaoSeguranca {

    @Bean
    SecurityFilterChain filtroDeSeguranca(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint naoAutenticado = (requisicao, resposta, erro) -> escrever(resposta, 401,
                "NAO_AUTENTICADO", "É preciso entrar para acessar este recurso.");
        AccessDeniedHandler acessoNegado = (requisicao, resposta, erro) -> escrever(resposta, 403,
                "ACESSO_NEGADO", "Você não tem permissão para acessar este recurso.");

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/saude").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/acesso/cadastro", "/api/acesso/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/barbeiro/**").hasRole("BARBEIRO")
                .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o
                .jwt(j -> j.jwtAuthenticationConverter(conversorDeToken()))
                .authenticationEntryPoint(naoAutenticado)
                .accessDeniedHandler(acessoNegado))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(naoAutenticado)
                .accessDeniedHandler(acessoNegado));
        return http.build();
    }

    /** O perfil que vem no token vira a autoridade ROLE_<PERFIL>. */
    private JwtAuthenticationConverter conversorDeToken() {
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(jwt -> {
            String perfil = jwt.getClaimAsString("perfil");
            Collection<GrantedAuthority> autoridades = perfil == null
                    ? List.of()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
            return autoridades;
        });
        return conversor;
    }

    /** Mesmo formato de ErroResposta; as mensagens fixas não têm aspas, então o JSON é seguro. */
    private static void escrever(HttpServletResponse resposta, int status, String codigo, String mensagem)
            throws IOException {
        resposta.setStatus(status);
        resposta.setContentType("application/json;charset=UTF-8");
        resposta.getWriter().write(
                "{\"status\":%d,\"codigo\":\"%s\",\"mensagem\":\"%s\",\"campos\":[],\"momento\":\"%s\"}"
                        .formatted(status, codigo, mensagem, Instant.now()));
    }

    @Bean
    PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean("corsConfigurationSource")
    CorsConfigurationSource configuracaoCors(
            @Value("${barberhub.cors.origens}") List<String> origens) {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(origens);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }
}