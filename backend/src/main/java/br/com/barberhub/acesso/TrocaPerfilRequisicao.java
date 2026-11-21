package br.com.barberhub.acesso;

import jakarta.validation.constraints.NotNull;

public record TrocaPerfilRequisicao(
        @NotNull(message = "Informe o perfil.")
        Perfil perfil) {
}
