package br.com.barberhub.acesso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefinirSenhaAdministrador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefinirSenhaAdministrador.class);

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder codificador;

    @Value("${barberhub.admin.senha-inicial:}")
    private String senhaInicial;

    @Override
    public void run(ApplicationArguments args) {
        usuarios.findByEmailIgnoreCase(Usuario.EMAIL_ADMINISTRADOR_INICIAL)
                .filter(admin -> admin.getSenhaHash() == null)
                .ifPresent(admin -> {
                    if (senhaInicial == null || senhaInicial.length() < 8) {
                        log.warn("O administrador está sem senha: defina ADMIN_SENHA_INICIAL (mínimo de 8 caracteres).");
                        return;
                    }
                    admin.setSenhaHash(codificador.encode(senhaInicial));
                    usuarios.save(admin);
                    log.info("Senha inicial do administrador definida.");
                });
    }
}