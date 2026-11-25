package br.com.barberhub.barbeiro;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FolgaBarbeiroRepositorio extends JpaRepository<FolgaBarbeiro, Long> {

    boolean existsByBarbeiroIdAndData(Long barbeiroId, LocalDate data);

    List<FolgaBarbeiro> findByBarbeiroIdAndDataGreaterThanEqualOrderByDataAsc(Long barbeiroId, LocalDate data);

    Optional<FolgaBarbeiro> findByIdAndBarbeiroId(Long id, Long barbeiroId);
}
