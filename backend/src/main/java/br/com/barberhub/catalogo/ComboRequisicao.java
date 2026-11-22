package br.com.barberhub.catalogo;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados para criar ou alterar um combo: nome, preço próprio e os serviços que o compõem. */
public record ComboRequisicao(
        @NotBlank(message = "Informe o nome do combo.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotNull(message = "Informe o preço.")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais.")
        BigDecimal preco,

        @NotEmpty(message = "Informe ao menos 2 serviços para o combo.")
        @Size(min = 2, message = "Informe ao menos 2 serviços para o combo.")
        List<Long> servicoIds) {
}