package br.com.barberhub.barbeiro;

import java.time.LocalDate;

public record FolgaResposta(Long id, LocalDate data, String motivo) {

    static FolgaResposta de(FolgaBarbeiro f) {
        return new FolgaResposta(f.getId(), f.getData(), f.getMotivo());
    }
}
