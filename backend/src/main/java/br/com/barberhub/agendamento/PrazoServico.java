package br.com.barberhub.agendamento;

import java.time.Clock;
import java.time.Duration;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Antecedência mínima para cancelar e remarcar (RN11), lida da configuração a cada uso. */
@Service
@RequiredArgsConstructor
public class PrazoServico {

    static final String CHAVE = "antecedencia_minima_horas";

    private final ConfiguracaoRepositorio configuracoes;
    private final Clock relogio;

    public int horasDeAntecedencia() {
        return configuracoes.findById(CHAVE)
                .map(c -> Integer.valueOf(c.getValor().trim()))
                .orElseThrow(() -> new IllegalStateException("Falta a configuração " + CHAVE));
    }

    /** Só o que está agendado muda, e exatamente no limite ainda pode. */
    public boolean podeAlterar(Agendamento agendamento, int horas) {
        return agendamento.getEstado() == EstadoAgendamento.AGENDADO
                && !relogio.instant().plus(Duration.ofHours(horas)).isAfter(agendamento.getInicio());
    }
}
