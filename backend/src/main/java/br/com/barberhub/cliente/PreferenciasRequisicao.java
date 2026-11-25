package br.com.barberhub.cliente;

import jakarta.validation.constraints.Size;

/** Todos opcionais: o que vier vazio é limpo. */
public record PreferenciasRequisicao(
        Long barbeiroFavoritoId,

        @Size(max = 120, message = "O tipo de corte deve ter no máximo 120 caracteres.")
        String tipoCorte,

        @Size(max = 2000, message = "As observações devem ter no máximo 2000 caracteres.")
        String observacoes) {
}
