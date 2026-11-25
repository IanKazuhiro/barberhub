package br.com.barberhub.agendamento;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.barbeiro.BarbeiroRepositorio;
import br.com.barberhub.barbeiro.EscalaBarbeiroRepositorio;
import br.com.barberhub.barbeiro.FolgaBarbeiroRepositorio;
import br.com.barberhub.catalogo.Servico;
import br.com.barberhub.catalogo.ServicoRepositorio;
import br.com.barberhub.compartilhado.erro.ConflitoException;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import lombok.RequiredArgsConstructor;

/**
 * Horários livres de um barbeiro (RF-AGE-02): dentro do expediente da barbearia, dentro da escala
 * dele, fora das folgas (RN07) e sem encostar em outro agendamento (RN01), com o serviço inteiro
 * cabendo antes do fim (RN02). Todas as contas usam o fuso da barbearia.
 */
@Service
@RequiredArgsConstructor
public class DisponibilidadeServico {

    static final String SERVICO_INDISPONIVEL = "Este serviço não está disponível.";
    static final String BARBEIRO_INDISPONIVEL = "Este barbeiro não está disponível.";
    static final String HORARIO_INVALIDO = "Escolha um dos horários oferecidos.";
    static final String HORARIO_OCUPADO = "Este horário acabou de ser ocupado. Escolha outro.";

    static final int PASSO_EM_MINUTOS = 15;
    static final int DIAS_A_FRENTE = 30;

    private final ServicoRepositorio servicos;
    private final UsuarioRepositorio usuarios;
    private final BarbeiroRepositorio barbeiros;
    private final EscalaBarbeiroRepositorio escalas;
    private final FolgaBarbeiroRepositorio folgas;
    private final ExpedienteRepositorio expedientes;
    private final AgendamentoRepositorio agendamentos;
    private final Clock relogio;

    private record Janela(LocalDateTime inicio, LocalDateTime fim) {

        boolean comporta(LocalDateTime de, LocalDateTime ate) {
            return !de.isBefore(inicio) && !ate.isAfter(fim);
        }
    }

    @Transactional(readOnly = true)
    public List<LocalTime> horariosLivres(Long barbeiroId, Long servicoId, LocalDate data) {
        Servico servico = servicoAtivo(servicoId);
        exigirBarbeiroAtivo(barbeiroId);
        return horariosLivres(barbeiroId, servico.getDuracaoMinutos(), data, null);
    }

    /** `ignorarId` é o agendamento que está sendo remarcado: ele não pode ocupar o lugar dele mesmo. */
    List<LocalTime> horariosLivres(Long barbeiroId, int duracao, LocalDate data, Long ignorarId) {
        if (foraDoPeriodo(data)) {
            return List.of();
        }
        LocalDateTime agora = LocalDateTime.now(relogio);
        List<Agendamento> ocupados = ocupados(barbeiroId, data.atStartOfDay(), data.plusDays(1).atStartOfDay(), ignorarId);

        List<LocalTime> livres = new ArrayList<>();
        for (Janela janela : janelas(barbeiroId, data)) {
            LocalDateTime inicio = proximoDaGrade(janela.inicio());
            while (janela.comporta(inicio, inicio.plusMinutes(duracao))) {
                if (inicio.isAfter(agora) && !colide(inicio, inicio.plusMinutes(duracao), ocupados)) {
                    livres.add(inicio.toLocalTime());
                }
                inicio = inicio.plusMinutes(PASSO_EM_MINUTOS);
            }
        }
        return livres;
    }

    /**
     * Confere de novo na hora de gravar, sem confiar no que a tela mostrou. Horário que nunca poderia
     * ser oferecido dá 422; horário válido que outra pessoa já pegou dá 409.
     */
    void exigirLivre(Long barbeiroId, int duracao, LocalDateTime inicio, Long ignorarId) {
        LocalDateTime fim = inicio.plusMinutes(duracao);
        boolean oferecivel = inicio.equals(proximoDaGrade(inicio))
                && inicio.isAfter(LocalDateTime.now(relogio))
                && !foraDoPeriodo(inicio.toLocalDate())
                && janelas(barbeiroId, inicio.toLocalDate()).stream().anyMatch(j -> j.comporta(inicio, fim));
        if (!oferecivel) {
            throw new ExcecaoNegocio(HORARIO_INVALIDO);
        }
        if (!ocupados(barbeiroId, inicio, fim, ignorarId).isEmpty()) {
            throw new ConflitoException(HORARIO_OCUPADO);
        }
    }

    Servico servicoAtivo(Long servicoId) {
        return servicos.findById(servicoId)
                .filter(Servico::isAtivo)
                .orElseThrow(() -> new ExcecaoNegocio(SERVICO_INDISPONIVEL));
    }

    Usuario exigirBarbeiroAtivo(Long barbeiroId) {
        return usuarios.findById(barbeiroId)
                .filter(u -> u.isAtivo() && u.getPerfil() == Perfil.BARBEIRO && barbeiros.existsById(u.getId()))
                .orElseThrow(() -> new ExcecaoNegocio(BARBEIRO_INDISPONIVEL));
    }

    Instant instante(LocalDateTime local) {
        return local.atZone(relogio.getZone()).toInstant();
    }

    LocalDateTime local(Instant instante) {
        return LocalDateTime.ofInstant(instante, relogio.getZone());
    }

    private boolean foraDoPeriodo(LocalDate data) {
        LocalDate hoje = LocalDate.now(relogio);
        return data.isBefore(hoje) || data.isAfter(hoje.plusDays(DIAS_A_FRENTE));
    }

    /** Cruza o expediente da barbearia com cada intervalo da escala do barbeiro naquele dia. */
    private List<Janela> janelas(Long barbeiroId, LocalDate data) {
        short dia = (short) (data.getDayOfWeek().getValue() % 7);
        ExpedienteBarbearia expediente = expedientes.findByDiaSemana(dia).orElse(null);
        if (expediente == null || expediente.isFechado() || folgas.existsByBarbeiroIdAndData(barbeiroId, data)) {
            return List.of();
        }
        return escalas.findByBarbeiroIdAndDiaSemanaOrderByInicioAsc(barbeiroId, dia).stream()
                .map(e -> new Janela(
                        data.atTime(maisTarde(e.getInicio(), expediente.getAbertura())),
                        data.atTime(maisCedo(e.getFim(), expediente.getFechamento()))))
                .filter(j -> j.inicio().isBefore(j.fim()))
                .toList();
    }

    private List<Agendamento> ocupados(Long barbeiroId, LocalDateTime de, LocalDateTime ate, Long ignorarId) {
        return agendamentos.ocupando(barbeiroId, instante(de), instante(ate)).stream()
                .filter(a -> !a.getId().equals(ignorarId))
                .toList();
    }

    private boolean colide(LocalDateTime de, LocalDateTime ate, List<Agendamento> ocupados) {
        Instant inicio = instante(de);
        Instant fim = instante(ate);
        return ocupados.stream().anyMatch(a -> a.getInicio().isBefore(fim) && a.getFim().isAfter(inicio));
    }

    /** 09:10 -> 09:15; 09:15 continua 09:15. Compara até os segundos, para 09:15:30 não passar. */
    private static LocalDateTime proximoDaGrade(LocalDateTime momento) {
        LocalDateTime base = momento.withMinute(momento.getMinute() / PASSO_EM_MINUTOS * PASSO_EM_MINUTOS)
                .withSecond(0).withNano(0);
        return base.equals(momento) ? base : base.plusMinutes(PASSO_EM_MINUTOS);
    }

    private static LocalTime maisTarde(LocalTime a, LocalTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalTime maisCedo(LocalTime a, LocalTime b) {
        return a.isBefore(b) ? a : b;
    }
}
