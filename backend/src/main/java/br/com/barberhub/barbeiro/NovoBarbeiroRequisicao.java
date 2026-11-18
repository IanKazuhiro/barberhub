package br.com.barberhub.barbeiro;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NovoBarbeiroRequisicao(
        @NotBlank(message = "Informe o nome.")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres.")
        String email,

        @NotBlank(message = "Informe o telefone.")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter 10 ou 11 dígitos, só números.")
        String telefone,

        @NotBlank(message = "Informe a senha inicial.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String senha,

        /** Opcional: sem valor, a comissão começa em 0% (o UC17 passa a editá-la). */
        @DecimalMin(value = "0.00", message = "A comissão não pode ser negativa.")
        @DecimalMax(value = "100.00", message = "A comissão não pode passar de 100%.")
        BigDecimal percentualComissao) {
}