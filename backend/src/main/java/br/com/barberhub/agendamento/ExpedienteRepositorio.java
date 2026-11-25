package br.com.barberhub.agendamento;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpedienteRepositorio extends JpaRepository<ExpedienteBarbearia, Long> {

    Optional<ExpedienteBarbearia> findByDiaSemana(short diaSemana);
}
