package br.com.barberhub.catalogo;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados para criar ou alterar um serviço. */
public record ServicoRequisicao(
        @NotBlank(message = "Informe o nome do serviço.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotNull(message = "Informe a duração em minutos.")
        @Min(value = 1, message = "A duração deve ser de pelo menos 1 minuto.")
        @Max(value = 480, message = "A duração deve ser de no máximo 480 minutos (8 horas).")
        Integer duracaoMinutos,

        @NotNull(message = "Informe o preço.")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais.")
        BigDecimal preco) {
}