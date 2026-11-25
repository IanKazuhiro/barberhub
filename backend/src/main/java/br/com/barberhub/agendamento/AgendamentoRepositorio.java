package br.com.barberhub.agendamento;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgendamentoRepositorio extends JpaRepository<Agendamento, Long> {

    /** O que ocupa a agenda do barbeiro no período. Cancelado e falta não ocupam (mesma regra da RN01 no banco). */
    @Query("""
            select a from Agendamento a
            where a.barbeiroId = :barbeiroId
              and a.estado in (br.com.barberhub.agendamento.EstadoAgendamento.AGENDADO,
                               br.com.barberhub.agendamento.EstadoAgendamento.CONCLUIDO)
              and a.inicio < :fim and a.fim > :inicio
            """)
    List<Agendamento> ocupando(@Param("barbeiroId") Long barbeiroId, @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);

    @Query("""
            select new br.com.barberhub.agendamento.AgendamentoDetalhado(a, s.nome, u.nome)
            from Agendamento a
            join Servico s on s.id = a.servicoId
            join Usuario u on u.id = a.barbeiroId
            where a.clienteId = :clienteId
            """)
    List<AgendamentoDetalhado> doCliente(@Param("clienteId") Long clienteId);
}
