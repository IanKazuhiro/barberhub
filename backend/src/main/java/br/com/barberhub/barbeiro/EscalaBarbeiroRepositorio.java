package br.com.barberhub.barbeiro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EscalaBarbeiroRepositorio extends JpaRepository<EscalaBarbeiro, Long> {

    List<EscalaBarbeiro> findByBarbeiroIdOrderByDiaSemanaAscInicioAsc(Long barbeiroId);

    List<EscalaBarbeiro> findByBarbeiroIdAndDiaSemanaOrderByInicioAsc(Long barbeiroId, short diaSemana);

    boolean existsByBarbeiroId(Long barbeiroId);

    @Query("select distinct e.barbeiroId from EscalaBarbeiro e")
    List<Long> idsDosBarbeirosComEscala();

    @Modifying
    @Query("delete from EscalaBarbeiro e where e.barbeiroId = :barbeiroId")
    void apagarDoBarbeiro(@Param("barbeiroId") Long barbeiroId);
}
