package br.com.barberhub.agendamento;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracaoRepositorio extends JpaRepository<Configuracao, String> {
}
