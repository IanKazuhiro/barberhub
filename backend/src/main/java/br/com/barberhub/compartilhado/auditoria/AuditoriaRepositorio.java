package br.com.barberhub.compartilhado.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepositorio extends JpaRepository<Auditoria, Long> {
}