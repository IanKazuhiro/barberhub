package br.com.barberhub.compartilhado.tempo;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Relógio no fuso da barbearia (RNF10). Os testes usam um relógio parado no lugar dele. */
@Configuration
public class ConfiguracaoRelogio {

    @Bean
    Clock relogio(@Value("${barberhub.fuso}") String fuso) {
        return Clock.system(ZoneId.of(fuso));
    }
}
