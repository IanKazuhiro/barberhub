package br.com.barberhub.barbeiro;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlterarBarbeiroRequisicao(
        @NotBlank(message = "Informe o nome.")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe o telefone.")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter 10 ou 11 dígitos, só números.")
        String telefone,

        @NotNull(message = "Informe a comissão.")
        @DecimalMin(value = "0.00", message = "A comissão não pode ser negativa.")
        @DecimalMax(value = "100.00", message = "A comissão não pode passar de 100%.")
        @Digits(integer = 3, fraction = 2, message = "A comissão aceita no máximo 2 casas decimais.")
        BigDecimal percentualComissao) {
}
