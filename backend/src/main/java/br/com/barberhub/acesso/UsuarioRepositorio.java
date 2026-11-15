package br.com.barberhub.acesso;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);
}