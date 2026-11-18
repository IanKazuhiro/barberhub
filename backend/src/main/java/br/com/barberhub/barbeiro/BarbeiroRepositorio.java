package br.com.barberhub.barbeiro;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BarbeiroRepositorio extends JpaRepository<Barbeiro, Long> {
}