package br.com.barberhub.agendamento;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AgendamentoRepositorio extends JpaRepository<Agendamento, Long> {

    Optional<Agendamento> findByIdAndClienteId(Long id, Long clienteId);

    /** Como findByIdAndClienteId, mas trava a linha até o fim da transação. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agendamento a where a.id = :id and a.clienteId = :clienteId")
    Optional<Agendamento> travarDoCliente(@Param("id") Long id, @Param("clienteId") Long clienteId);

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

    /** Do mais recente para o mais antigo. */
    @Query("""
            select new br.com.barberhub.agendamento.AgendamentoDetalhado(a, s.nome, u.nome)
            from Agendamento a
            join Servico s on s.id = a.servicoId
            join Usuario u on u.id = a.barbeiroId
            where a.clienteId = :clienteId and a.estado = :estado
            order by a.inicio desc, a.id desc
            """)
    List<AgendamentoDetalhado> doClientePorEstado(@Param("clienteId") Long clienteId,
            @Param("estado") EstadoAgendamento estado);

    @Query("""
            select new br.com.barberhub.agendamento.AgendamentoComCliente(
                a, u.nome, u.telefone, c.tipoCorte, c.observacoes, s.nome)
            from Agendamento a
            join Usuario u on u.id = a.clienteId
            join Cliente c on c.usuarioId = a.clienteId
            join Servico s on s.id = a.servicoId
            where a.barbeiroId = :barbeiroId and a.inicio >= :de and a.inicio < :ate
            order by a.inicio, a.id
            """)
    List<AgendamentoComCliente> agendaDoBarbeiro(@Param("barbeiroId") Long barbeiroId, @Param("de") Instant de,
            @Param("ate") Instant ate);
}
